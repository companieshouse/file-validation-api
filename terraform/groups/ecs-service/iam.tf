resource "aws_iam_role" "ecs_task_role" {
  name               = "${local.name_prefix}-${local.service_name}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_policy.json
}

data "aws_iam_policy_document" "ecs_task_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "ecs-tasks.amazonaws.com"
      ]
    }
  }
}

data "aws_iam_policy_document" "bucket_access_policy" {
  statement {
    sid = "S3Permissions"

    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:ListObject"
    ]

    resources = [
      "arn:aws:s3:::${var.environment}-acsp-aml-data-chips-data/*",
      "arn:aws:s3:::${var.environment}-acsp-aml-data-chips-data"
    ]
  }
}

resource "aws_iam_role_policy" "bucket_access_policy" {
  name   = "bucket-access-role-policy"
  role   = aws_iam_role.ecs_task_role.id
  policy = data.aws_iam_policy_document.bucket_access_policy.json
}
