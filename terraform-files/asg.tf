resource "aws_autoscaling_group" "ecs_asg" {
  vpc_zone_identifier = [aws_subnet.subnet.id, aws_subnet.subnet2.id]
  desired_capacity    = 1
  max_size            = 3
  min_size            = 1
  force_delete = true

  launch_template {
    id      = aws_launch_template.ttt_ecs_lt.id
    version = "$Latest"
  }
  tag {
    key                 = "AmazonECSManaged"
    value               = true
    propagate_at_launch = true
  }
  tag {
    key                 = "Name"
    value               = "tictactoe-ecs-instance"
    propagate_at_launch = true
  }
  tag {
    key                 = "aws:ecs:cluster-name"
    value               = aws_ecs_cluster.tictactoe_cluster.name
    propagate_at_launch = true
  }

}