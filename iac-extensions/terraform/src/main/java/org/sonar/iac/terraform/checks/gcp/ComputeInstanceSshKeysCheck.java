/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.checks.gcp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static org.sonar.iac.common.checks.PropertyUtils.get;
import static org.sonar.iac.common.checks.PropertyUtils.valueOrNull;
import static org.sonar.iac.terraform.api.tree.TerraformTree.Kind.BOOLEAN_LITERAL;
import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessToString;

@Rule(key = "S6405")
public class ComputeInstanceSshKeysCheck extends AbstractNewResourceCheck {

  private static final String MESSAGE = "Make sure that enabling project-wide SSH keys is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting metadata.block-project-ssh-keys enables project-wide SSH keys. Make sure it is safe here.";

  private final TemplatesCollector collector = new TemplatesCollector("google_compute_instance_template");

  @Override
  public void initialize(InitContext init) {
    super.initialize(init);
    init.register(FileTree.class, (ctx, tree) -> collector.scan(new TreeContext(), tree));
  }

  @Override
  protected void registerResourceConsumer() {
    register("google_compute_instance",
      resource -> checkMetadata(resource, true));

    register("google_compute_instance_from_template",
      this::checkInstanceFromTemplate);
  }

  private void checkInstanceFromTemplate(ResourceSymbol resource) {
    AttributeSymbol srcTemplate = resource.attribute("source_instance_template");
    srcTemplate.reportIfAbsent("Missing source_instance_template reference.");
    if (srcTemplate.isPresent()) {
      Tree srcTemplateValue = srcTemplate.tree.value();
      if (!(srcTemplateValue instanceof AttributeAccessTree)) {
        srcTemplate.ctx.reportIssue(srcTemplateValue, "Template reference of unexpected type.");
        checkMetadata(resource, true);
        return;
      }
      // Now it is safe to cast:
      String templateId = attributeAccessToString((AttributeAccessTree) srcTemplateValue);
      Boolean compliantTemplate = collector.collectedTemplates.get(templateId);
      if (compliantTemplate == null) {
        srcTemplate.report("Reference to missing template.");
        checkMetadata(resource, true);
      } else if (compliantTemplate) {
        // It's a Compliant template but still the local definition has a chance of spoiling it:
        checkMetadata(resource, false);
      } else {
        // It's a Noncompliant template but still the local definition has a chance of fixing it:
        if (checkMetadata(resource, false) != Trilean.TRUE) {
          Tree highlight = collector.sensitiveTemplatesTree.get(templateId);
          srcTemplate.report(MESSAGE, List.of(new SecondaryLocation(highlight, "specified here")));
        }
      }
    }
  }

  /**
   * @param resource the target resource (of type 'google_compute_instance' or 'google_compute_instance_from_template')
   * @param reportOnUndefined should we report in case of undefined 'block-project-ssh-keys' (or part of its path)?
   * @return Trilean.TRUE    iff resource["metadata"]["block-project-ssh-keys"] is defined and == true
   *         Trilean.FALSE   iff resource["metadata"]["block-project-ssh-keys"] is defined but != true
   *         Trilean.UNKNWON iff resource["metadata"]["block-project-ssh-keys"] (or part of it) is not defined
   */
  private Trilean checkMetadata(ResourceSymbol resource, boolean reportOnUndefined) {
    AttributeSymbol metadata = resource.attribute("metadata");

    if (metadata.isAbsent()) {
      if (reportOnUndefined) {
        metadata.reportIfAbsent(OMITTING_MESSAGE);
      }
      return Trilean.UNKNOWN;
    }

    if (!(metadata.tree.value() instanceof ObjectTree)) {
      if (reportOnUndefined) {
        metadata.report("metadata of type Object expected.");
      }
      return Trilean.UNKNOWN;
    }

    var metadataObj = (ObjectTree) metadata.tree.value();
    Tree sshKeysValue = valueOrNull(metadataObj, "block-project-ssh-keys");
    if (sshKeysValue == null) {
      if (reportOnUndefined) {
        metadata.report(OMITTING_MESSAGE);
      }
      return Trilean.UNKNOWN;
    }

    if (sshKeysValue instanceof TextTree && ((TextTree) sshKeysValue).value().equalsIgnoreCase("true")) {
      return Trilean.TRUE;
    } else {
      metadata.ctx.reportIssue(sshKeysValue, MESSAGE);
      return Trilean.FALSE;
    }
  }

  static class TemplatesCollector extends TreeVisitor<TreeContext> {
    /** Maps template id to:
     *  TRUE if Compliant template
     *  FALSE if Noncompliant template
     *  null if no such template
     */
    private final Map<String, Boolean> collectedTemplates = new HashMap<>();

    /** Maps only Noncompliant template id to its corresponding Highlight */
    private final Map<String, Tree> sensitiveTemplatesTree = new HashMap<>();

    private final Set<String> relevantResourceTypes;

    public TemplatesCollector(String... relevantResourceTypes) {
      this.relevantResourceTypes = Set.of(relevantResourceTypes);
      register(BlockTree.class, (ctx, tree) -> {
        if (isResource(tree) && this.relevantResourceTypes.contains(resourceType(tree))) {
          collectTemplate(tree);
        }
      });
    }

    /** Maps sensitive template's key to its corresponding hotspot (i.e. the Tree to highlight)
     *  Does nothing on non-sensitive templates (i.e. no mapping is created)
     */
    protected void collectTemplate(BlockTree resourceBlock) {
      if (resourceBlock.labels().size() <= 1) {
        return; // unnamed template can't be referenced
      }
      String templateName = resourceBlock.labels().get(1).value();
      String templateId = String.format("google_compute_instance_template.%s.id", templateName);

      Tree highlight = resourceBlock.key();
      ObjectTree metadataObj = valueOrNull(resourceBlock, "metadata", ObjectTree.class);
      if (metadataObj != null) {
        highlight = metadataObj;
        ObjectElementTree sshKeysAttr = get(metadataObj, "block-project-ssh-keys", ObjectElementTree.class).orElse(null);
        if (sshKeysAttr != null) {
          highlight = sshKeysAttr;
          if (sshKeysAttr.value().is(BOOLEAN_LITERAL) && ((LiteralExprTree) sshKeysAttr.value()).token().value().equalsIgnoreCase("true")) {
            collectedTemplates.put(templateId, Boolean.TRUE); // Compliant
            return;
          }
        }
      }

      collectedTemplates.put(templateId, Boolean.FALSE); // Noncompliant
      sensitiveTemplatesTree.put(templateId, highlight); // Highlight area
    }

    /** Given a template id (in the form of google_compute_instance_template.XXX.id)
     * returns that template's highlight Tree if the template is sensitive
     * or null if it's non-sensitive or no such template
     */
    public Tree getSensitiveTemplateHighlightArea(String templateId) {
      return sensitiveTemplatesTree.get(templateId);
    }
  }
}
