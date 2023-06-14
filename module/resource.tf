resource "aws_instance" "server1" {
  #instance_type = var.instance_type[count.index]
  instance_type               = lookup(var.static, "itype")
  ami                         = lookup(var.static, "ami")
  associate_public_ip_address = lookup(var.static, "publicip")
  key_name                    = lookup(var.static, "keyname")
  
  tags = {
    Name = var.inst_name
  }
}