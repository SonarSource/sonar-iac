# SHARED CANDIDATE???
# Does every squad/team has the same definition of main branch?
def is_sonarsource_qa():
    """
    Is the current branch a branch for which the QA should be executed?

    :return: an expression to be used in the only_if task parameter
    """
    return "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_PR != \"\" || $CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\" || $CIRRUS_BRANCH =~ \"dogfood-on-.*\")"


# SHARED CANDIDATE
def is_main_branch():
    """
    Is the current branch the main branch?

    :return: an expression to be used in the only_if task parameter
    """
    return "$CIRRUS_USER_COLLABORATOR == 'true' && $CIRRUS_TAG == \"\" && ($CIRRUS_BRANCH == $CIRRUS_DEFAULT_BRANCH || $CIRRUS_BRANCH =~ \"branch-.*\")"
