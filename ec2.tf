resource "aws_launch_template" "ecs_lt" {
  name_prefix   = "ecs-template"
  image_id      = "ami-0c101f26f147fa7fd"
  instance_type = "t2.medium"

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
    tags = {
      Name = "tictactoe-ecs-instance"
    }
  }

#  user_data = filebase64("${path.module}/ecs.sh")
  user_data = <<-EOF
              #!/bin/bash
              echo ECS_CLUSTER=${aws_ecs_cluster.tictactoe_cluster.name} >> /etc/ecs/ecs.config
              yum install -y aws-cli
              EOF
}