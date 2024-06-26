# 1. Create vpc

resource "aws_vpc" "prod-vpc" {
  cidr_block = "10.0.0.0/16"
  tags = {
    Name = "production"
  }
}

# 2. Create Internet Gateway

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.prod-vpc.id


}
# 3. Create Custom Route Table

resource "aws_route_table" "prod-route-table" {
  vpc_id = aws_vpc.prod-vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  route {
    ipv6_cidr_block = "::/0"
    gateway_id      = aws_internet_gateway.gw.id
  }

  tags = {
    Name = "Prod"
  }
}

# 4. Create a Subnet

resource "aws_subnet" "subnet-1" {
  vpc_id            = aws_vpc.prod-vpc.id
#  cidr_block        = "10.0.1.0/24"
  cidr_block              = cidrsubnet(aws_vpc.prod-vpc.cidr_block, 8, 1)
  availability_zone = "us-east-1a"
  map_public_ip_on_launch = true

  tags = {
    Name = "prod-subnet1"
  }
}

resource "aws_subnet" "subnet-2" {
  vpc_id            = aws_vpc.prod-vpc.id
  cidr_block              = cidrsubnet(aws_vpc.prod-vpc.cidr_block, 8, 2)
  availability_zone = "us-east-1b"
  map_public_ip_on_launch = true

  tags = {
    Name = "prod-subnet2"
  }
}

# 5. Associate subnet with Route Table
resource "aws_route_table_association" "a" {
  subnet_id      = aws_subnet.subnet-1.id
  route_table_id = aws_route_table.prod-route-table.id
}
resource "aws_route_table_association" "b" {
  subnet_id      = aws_subnet.subnet-2.id
  route_table_id = aws_route_table.prod-route-table.id
}
# 6. Create Security Group to allow port 22,80,443,8080
resource "aws_security_group" "allow_web" {
  name        = "allow_web_traffic"
  description = "Allow Web inbound traffic"
  vpc_id      = aws_vpc.prod-vpc.id

#  ingress {
#    description = "Backend"
#    from_port   = 8080
#    to_port     = 8080
#    protocol    = "tcp"
#    cidr_blocks = ["10.0.0.0/16"]
#  }
#  ingress {
#    description = "HTTPS"
#    from_port   = 443
#    to_port     = 443
#    protocol    = "tcp"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
#  ingress {
#    description = "HTTP"
#    from_port   = 80
#    to_port     = 80
#    protocol    = "tcp"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
#  ingress {
#    description = "SSH"
#    from_port   = 22
#    to_port     = 22
#    protocol    = "tcp"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
  ingress {
    description = "temp allow all"
        from_port   = 0
        to_port     = 0
        protocol    = "-1"
        cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "allow_web"
  }
}

# 7. Create a network interface with an ip in the subnet that was created in step 4

resource "aws_network_interface" "web-server-nic" {
  subnet_id       = aws_subnet.subnet-1.id
  private_ips     = ["10.0.1.50"]
  security_groups = [aws_security_group.allow_web.id]

}
# 8. Assign an elastic IP to the network interface created in step 7

 resource "aws_eip" "one" {
   domain                       = "vpc"
   network_interface         = aws_network_interface.web-server-nic.id
   associate_with_private_ip = "10.0.1.50"
   depends_on                = [aws_internet_gateway.gw]
 }

 output "server_public_ip" {
   value = aws_eip.one.public_ip
 }

# 9. Create Ubuntu server and install/enable apache2

resource "aws_instance" "web-server-instance" {
  ami                  = "ami-0dc67873410203528" #ami-0dc67873410203528 #ami-0c101f26f147fa7fd
  instance_type        = "t2.medium"
  availability_zone    = "us-east-1a"
  key_name             = "vockey"
  iam_instance_profile = "LabInstanceProfile"
  #   vpc_security_group_ids = [aws_security_group.allow_web.id]
  network_interface {
    device_index         = 0
    network_interface_id = aws_network_interface.web-server-nic.id
  }

  user_data = <<-EOF
                 #!/bin/bash
                  sudo echo "ECS_CLUSTER=tictactoe-cluster" >> /etc/ecs/ecs.config
                 EOF
  #                 sudo yum update -y
  #   sudo yum install -y ecs-init
  # sudo yum install -y docker
  # sudo systemctl start docker.service
  # sudo systemctl start ecs.service
  # sudo usermod -a -G docker ec2-user
  #   sudo yum install apache2 -y
  # sudo systemctl start apache2
  # sudo bash -c 'echo your very first web server > /var/www/html/index.html'

  tags = {
    Name = "web-server"
  }
}


output "server_private_ip" {
  value = aws_instance.web-server-instance.private_ip

}

#output "server_public_ip" {
#  value = aws_instance.web-server-instance.public_ip
#
#}

output "server_id" {
  value = aws_instance.web-server-instance.id
}

#
# resource "<provider>_<resource_type>" "name" {
#     config options.....
#     key = "value"
#     key2 = "another value"
# }