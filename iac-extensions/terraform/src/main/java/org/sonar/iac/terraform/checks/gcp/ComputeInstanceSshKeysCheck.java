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
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.BlockSymbol;

import static org.sonar.iac.common.checks.PropertyUtils.valueOrNull;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;

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
      resource -> resource.block("metadata")
        .reportIfAbsent(OMITTING_MESSAGE)
        .attribute("block-project-ssh-keys")
          .reportIfAbsent(OMITTING_MESSAGE)
          .reportIf(isFalse(), MESSAGE));

    register("google_compute_instance_from_template",
      resource -> {
        BlockSymbol metadata = resource.block("metadata");
        if (metadata.isPresent()) {
          metadata.attribute("block-project-ssh-keys")
            .reportIfAbsent(OMITTING_MESSAGE)
            .reportIf(isFalse(), MESSAGE);
        } else {
          var src = resource.attribute("source_instance_template");
          src.reportIfAbsent(OMITTING_MESSAGE);
          if (src.isPresent()) {
            String templateKey = ((TextTree) src.tree).value();
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
      BlockTree metadataBlock = valueOrNull(resourceBlock, "metadata", BlockTree.class);
      if (metadataBlock != null) {
        highlight = metadataBlock;
        AttributeTree sshKeysAttr = valueOrNull(metadataBlock, "block-project-ssh-keys", AttributeTree.class);
        if (sshKeysAttr != null) {
          highlight = sshKeysAttr;
          if (TextUtils.isValueTrue(sshKeysAttr)) {
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
