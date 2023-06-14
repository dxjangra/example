variable "inst_name" {
  type    = string
  default = "Instance_pub"
}
variable "static" {
  type = map(any)
  default = {
    ami      = "ami-053b0d53c279acc90"
    publicip = true
    keyname  = "ninja"
    itype    = "t2.micro"
  }
}