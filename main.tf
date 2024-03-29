terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
  required_version = ">= 1.2.0"
}
provider "aws" {
  region              = "us-east-1"
  shared_config_files = ["C:/Users/Kuba/.aws/config"]
  #  shared_credentials_files = ["C:/Users/Kuba/.aws/creds"]
  #  profile                  = "customprofile"
}
#resource "aws_instance" "web" {
#  ami           = "ami-0c101f26f147fa7fd"
#  instance_type = "t2.micro"
#
#  tags = {
#    Name = "TicTacToe Cloud"
#  }
#  # Launch the instances within the same subnet as the ECS cluster
#  subnet_id = aws_subnet.subnet.id
#
#  # Assign the custom security group to the instances
#  security_groups = [aws_security_group.instance_sg.id]
#
#  # Example user data to install and configure ECS Container Agent
#  user_data = <<-EOF
#              #!/bin/bash
#              echo ECS_CLUSTER=${aws_ecs_cluster.tictactoe_cluster.name} >> /etc/ecs/ecs.config
#              yum install -y aws-cli
#              EOF
#}

#Define ECR images/repo
resource "aws_ecr_repository" "frontend-ecr-repo" {
  name = "new_front"
}
resource "aws_ecr_repository" "backend-ecr-repo" {
  name = "new_back"
}
# Define ECS cluster
resource "aws_ecs_cluster" "tictactoe_cluster" {
  name = "tictactoe-cluster"
}

# Define ECS task definition for frontend
resource "aws_ecs_task_definition" "frontend_task" {
  family             = "frontend-task"
  network_mode       = "awsvpc"
  execution_role_arn = "arn:aws:iam::139366017033:role/LabRole"
  cpu                = 512
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  container_definitions = jsonencode(
    [
      {
        name : "frontend",
        image : aws_ecr_repository.frontend-ecr-repo.repository_url,
        memory : 512,
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
  network_mode       = "awsvpc"
  execution_role_arn = "arn:aws:iam::139366017033:role/LabRole"
  cpu                = 512
  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }
  container_definitions = jsonencode(
    [
      {
        name : "backend",
        image : aws_ecr_repository.backend-ecr-repo.repository_url,
        memory : 1024,
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
    subnets         = [aws_subnet.subnet.id, aws_subnet.subnet2.id]
    security_groups = [aws_security_group.instance_sg.id]
  }
  force_new_deployment = true
  placement_constraints {
    type = "distinctInstance"
  }

  triggers = {
    redeployment = timestamp()
  }

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.ecs_capacity_provider.name
    weight            = 100
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.ecs_tg_ttt.arn
    container_name   = "frontend"
    container_port   = 80
  }

  depends_on = [aws_autoscaling_group.ecs_asg]
}

# Define ECS service for backend
resource "aws_ecs_service" "backend_service" {
  name            = "backend-service"
  cluster         = aws_ecs_cluster.tictactoe_cluster.id
  task_definition = aws_ecs_task_definition.backend_task.arn
  desired_count   = 1

  #  launch_type = "EC2"  # Specify the launch type as EC2

  network_configuration {
    subnets         = [aws_subnet.subnet.id, aws_subnet.subnet2.id]
    security_groups = [aws_security_group.instance_sg.id]
  }
  force_new_deployment = true
  placement_constraints {
    type = "distinctInstance"
  }

  triggers = {
    redeployment = timestamp()
  }

  capacity_provider_strategy {
    capacity_provider = aws_ecs_capacity_provider.ecs_capacity_provider.name
    weight            = 100
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.ecs_tg_ttt.arn
    container_name   = "backend"
    container_port   = 8080
  }

  depends_on = [aws_autoscaling_group.ecs_asg]
}


