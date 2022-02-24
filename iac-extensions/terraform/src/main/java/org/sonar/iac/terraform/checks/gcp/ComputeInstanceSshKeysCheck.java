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
import org.sonar.iac.common.checks.PropertyUtils;
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
      resource -> checkMetadata(resource.attribute("metadata")));

    register("google_compute_instance_from_template",
      resource -> {
        if (!checkMetadata(resource.attribute("metadata"))) {
          AttributeSymbol src = resource.attribute("source_instance_template");
          src.reportIfAbsent(OMITTING_MESSAGE);
          if (src.isPresent()) {
            String templateKey = attributeAccessToString((AttributeAccessTree) src.tree.value());
            Tree highlight = collector.getSensitiveTemplateHighlightArea(templateKey);
            if (highlight != null) {
              src.report(MESSAGE, List.of(new SecondaryLocation(highlight, "specified here")));
            } else {
              src.report("Invalid template reference");
            }
          }
        }
      });
  }

  private boolean checkMetadata(AttributeSymbol metadata) {
    if (metadata.isPresent()) {
      if (metadata.tree.value() instanceof ObjectTree) {
        var obj = (ObjectTree) metadata.tree.value();
        Tree sshKeysProperty = PropertyUtils.valueOrNull(obj, "block-project-ssh-keys");
        if (sshKeysProperty == null) {
          metadata.report(OMITTING_MESSAGE);
        } else if (!(sshKeysProperty instanceof TextTree)
          || !((TextTree) sshKeysProperty).value().equalsIgnoreCase("true")) {
          metadata.ctx.reportIssue(sshKeysProperty, MESSAGE);
        }
      } else {
        metadata.report("Object value expected.");
      }
      return true;
    } else {
      metadata.reportIfAbsent(OMITTING_MESSAGE);
      return false;
    }
  }

  static class TemplatesCollector extends TreeVisitor<TreeContext> {
    private final Map<String, Tree> sensitiveTemplates = new HashMap<>();
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

      Tree highlight = resourceBlock.key();
      ObjectTree metadataObj = valueOrNull(resourceBlock, "metadata", ObjectTree.class);
      if (metadataObj != null) {
        highlight = metadataObj;
        //LiteralExprTree sshKeysAttr = valueOrNull(metadataObj, "block-project-ssh-keys", LiteralExprTree.class);
        ObjectElementTree sshKeysAttr = get(metadataObj, "block-project-ssh-keys", ObjectElementTree.class).orElse(null);
        if (sshKeysAttr != null) {
          highlight = sshKeysAttr;
          if (sshKeysAttr.value().is(BOOLEAN_LITERAL) && ((LiteralExprTree) sshKeysAttr.value()).token().value().equalsIgnoreCase("true")) {
            return; // Compliant
          }
        }
      }
      String templateKey = String.format("google_compute_instance_template.%s.id", templateName);
      sensitiveTemplates.put(templateKey, highlight);
    }

    /** Given a template key ref in the form of google_compute_instance_template.XXX.id
     * returns that template's highlight Tree if the template is sensitive
     * or null if it's non-sensitive
     */
    public Tree getSensitiveTemplateHighlightArea(String templateKey) {
      return sensitiveTemplates.get(templateKey);
    }
  }
}
