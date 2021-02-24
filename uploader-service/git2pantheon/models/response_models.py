from marshmallow import Schema, fields, post_load


class Status():
    def __init__(self, current_status, file_type, last_uploaded_file, total_files_uploaded):
        self.current_status = current_status
        self.processing_file_type = file_type
        self.last_uploaded_file = last_uploaded_file
        self.total_files_uploaded = total_files_uploaded


class StatusSchema(Schema):
    current_status: fields.Str()
    processing_file_type: fields.Str()

    @post_load
    def make_status(self, data, **kwargs):
        return Status(**data)
