from marshmallow import Schema, fields, post_load


class RepoSchema(Schema):
    repo: fields.Str(required=True)
    branch: fields.Str(missing='master')

    @post_load
    def make_repo(self, data, **kwargs):
        return Repo(**data)


class Repo:
    def __init__(self, repo, branch):
        self.repo = repo
        self.branch = branch if branch else 'master'
