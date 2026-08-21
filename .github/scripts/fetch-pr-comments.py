#!/usr/bin/env python3
"""Fetch a pull request's metadata and review comments from the GitHub API.

Writes four files into the output directory (``/tmp`` by default):

* ``pr_title.txt``     — the PR title
* ``pr_head_ref.txt``  — the PR head branch name
* ``pr_head_repo.txt`` — the ``owner/repo`` the head branch lives in (differs for forks)
* ``pr_comments.txt``  — review-level, inline and general comments, formatted for reading

Both the ``address-pr-review-comments`` skill and
``.github/workflows/ai-address-review-comments.yml`` call this script, so the comments the
workflow reports and the comments Claude works from can never drift apart.
"""

import argparse
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.request

DEFAULT_REPO = "SonarSource/sonar-iac-enterprise"


def resolve_token():
    """Token from GITHUB_TOKEN, falling back to the gh CLI for local runs."""
    token = os.environ.get("GITHUB_TOKEN", "")
    if token:
        return token
    try:
        return subprocess.check_output(["gh", "auth", "token"], text=True).strip()
    except Exception:
        sys.exit(
            "ERROR: no GITHUB_TOKEN environment variable and `gh auth token` failed. "
            "Set GITHUB_TOKEN or run `gh auth login`."
        )


def make_getters(token):
    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json",
    }

    def request(url):
        try:
            return urllib.request.urlopen(urllib.request.Request(url, headers=headers))
        except urllib.error.HTTPError as error:
            sys.exit(f"ERROR: GET {url} returned HTTP {error.code}\n{error.read().decode()}")
        except urllib.error.URLError as error:
            sys.exit(f"ERROR: GET {url} failed: {error.reason}")

    def get(url):
        with request(url) as response:
            return json.loads(response.read())

    def get_all(url):
        """Follow RFC 5988 `rel="next"` links so long comment threads are not truncated."""
        results = []
        while url:
            with request(url) as response:
                results.extend(json.loads(response.read()))
                url = None
                for part in response.getheader("Link", "").split(","):
                    if 'rel="next"' in part:
                        match = re.search(r"<([^>]+)>", part)
                        if match:
                            url = match.group(1)
        return results

    return get, get_all


def format_comments(review_bodies, inline_comments, issue_comments):
    lines = []

    if review_bodies:
        lines.append("## Review-level comments\n")
        for review in review_bodies:
            body = (review.get("body") or "").strip()
            lines.append(f"**{review['user']['login']}** ({review['state']}):\n{body}\n")

    if inline_comments:
        lines.append("## Inline (line-level) comments\n")
        for comment in inline_comments:
            line = comment.get("line") or comment.get("original_line", "?")
            body = (comment.get("body") or "").strip()
            lines.append(
                f"**{comment['user']['login']}** on `{comment['path']}` (line {line}):\n{body}\n"
            )
            diff_hunk = comment.get("diff_hunk", "")
            if diff_hunk:
                lines.append(f"```diff\n{diff_hunk}\n```\n")

    if issue_comments:
        lines.append("## General PR comments\n")
        for comment in issue_comments:
            body = (comment.get("body") or "").strip()
            lines.append(f"**{comment['user']['login']}**:\n{body}\n")

    return "\n".join(lines) if lines else "No review comments found."


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pr_number", help="pull request number, e.g. 42")
    parser.add_argument(
        "--repo",
        default=os.environ.get("GITHUB_REPOSITORY", DEFAULT_REPO),
        help="owner/repo slug (default: $GITHUB_REPOSITORY, else %(default)s)",
    )
    parser.add_argument(
        "--out-dir", default="/tmp", help="directory to write the output files to (default: %(default)s)"
    )
    args = parser.parse_args()

    get, get_all = make_getters(resolve_token())
    base_url = f"https://api.github.com/repos/{args.repo}"

    pull_request = get(f"{base_url}/pulls/{args.pr_number}")
    head = pull_request["head"]
    # A fork's head repository can have been deleted, in which case `repo` is null.
    head_repo = (head.get("repo") or {}).get("full_name", "")

    inline_comments = get_all(f"{base_url}/pulls/{args.pr_number}/comments?per_page=100")
    reviews = get_all(f"{base_url}/pulls/{args.pr_number}/reviews?per_page=100")
    issue_comments = get_all(f"{base_url}/issues/{args.pr_number}/comments?per_page=100")

    # A bare approval carries no request to act on; a body-less review is only a container
    # for the inline comments, which are reported separately.
    review_bodies = [
        review
        for review in reviews
        if (review.get("body") or "").strip() and review["state"] != "APPROVED"
    ]

    outputs = {
        "pr_title.txt": pull_request["title"],
        "pr_head_ref.txt": head["ref"],
        "pr_head_repo.txt": head_repo,
        "pr_comments.txt": format_comments(review_bodies, inline_comments, issue_comments),
    }
    for name, content in outputs.items():
        with open(os.path.join(args.out_dir, name), "w") as handle:
            handle.write(content)

    print(f"PR #{args.pr_number}: {pull_request['title']}")
    print(f"Head: {head_repo}:{head['ref']}")
    print(
        f"Comments: {len(inline_comments)} inline, {len(review_bodies)} review-level, "
        f"{len(issue_comments)} general"
    )


if __name__ == "__main__":
    main()
