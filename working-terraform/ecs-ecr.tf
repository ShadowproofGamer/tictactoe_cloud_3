
#Define ECR images/repo
#resource "aws_ecr_repository" "frontend-ecr-repo" {
#  name = "new_front"
#}
#resource "aws_ecr_repository" "backend-ecr-repo" {
#  name = "new_back"
#}
# Define ECS cluster
resource "aws_ecs_cluster" "tictactoe_cluster" {
  name = "tictactoe-cluster"
}

# Define ECS task definition for frontend
resource "aws_ecs_task_definition" "frontend_task" {
  family             = "frontend-task"
  network_mode       = "host" #"awsvpc"
  execution_role_arn = "arn:aws:iam::139366017033:role/LabRole"
  cpu                = 512
  container_definitions = jsonencode(
    [
      {
        name : "frontend",
        image : "139366017033.dkr.ecr.us-east-1.amazonaws.com/new_front:latest",
        memory : 256,
        cpu : 512
        portMappings : [
          {
            "containerPort" : 80,
            "hostPort" : 80
          }
        ]
      }
  ])
}

# Define ECS task definition for backend
resource "aws_ecs_task_definition" "backend_task" {
  family             = "backend-task"
  network_mode       = "host" #"awsvpc"
  execution_role_arn = "arn:aws:iam::139366017033:role/LabRole"
  cpu                = 512
  container_definitions = jsonencode(
    [
      {
        name : "backend",
        image : "139366017033.dkr.ecr.us-east-1.amazonaws.com/new_back:latest",
        memory : 512,
        cpu : 512

        portMappings : [
          {
            "containerPort" : 8080,
            "hostPort" : 8080
          }
        ]
      }
  ])
}

# Define ECS service for frontend
resource "aws_ecs_service" "frontend_service" {
  name            = "frontend-service"
  cluster         = aws_ecs_cluster.tictactoe_cluster.id
  task_definition = aws_ecs_task_definition.frontend_task.arn
  desired_count   = 1

  #  launch_type = "EC2"  # Specify the launch type as EC2

  network_configuration {
    subnets         = [aws_subnet.subnet-1.id]
    security_groups = [aws_security_group.allow_web.id]
  }
}

# Define ECS service for backend
resource "aws_ecs_service" "backend_service" {
  name            = "backend-service"
  cluster         = aws_ecs_cluster.tictactoe_cluster.id
  task_definition = aws_ecs_task_definition.backend_task.arn
  desired_count   = 1

  #  launch_type = "EC2"  # Specify the launch type as EC2

  network_configuration {
    subnets         = [aws_subnet.subnet-1.id]
    security_groups = [aws_security_group.allow_web.id]
  }

}