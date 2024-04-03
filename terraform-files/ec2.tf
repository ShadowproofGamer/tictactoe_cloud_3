resource "aws_launch_template" "ttt_ecs_lt" {
  name_prefix   = "tictactoe-ecs-template"
  image_id      = "ami-0dc67873410203528"
  instance_type = "t2.micro"
  instance_initiated_shutdown_behavior = "terminate"

  key_name               = "vockey"
  vpc_security_group_ids = [aws_security_group.instance_sg.id]
  iam_instance_profile {
    name = "LabInstanceProfile"
  }

  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size = 30
      volume_type = "gp2"
    }
  }

  tag_specifications {
    resource_type = "instance"
    tags          = {
      Name = "tictactoe-ecs-instance"
    }
  }

  user_data = filebase64("${path.module}/ecs.sh") #"${path.module}/ecs.sh"
  #  user_data = <<-EOF
  #              #!/bin/bash
  #              sudo echo ECS_CLUSTER=${aws_ecs_cluster.tictactoe_cluster.name} >> /etc/ecs/ecs.config
  #              yum install -y aws-cli
  #              EOF
}