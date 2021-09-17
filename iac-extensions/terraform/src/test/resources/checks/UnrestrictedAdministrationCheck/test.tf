resource "aws_security_group" "all_protocols" {
  ingress {
    protocol         = -1
    #                  ^^> {{Related protocol setting}}
    cidr_blocks      = ["0.0.0.0/0"] # Noncompliant
    #                  ^^^^^^^^^^^^^
  }
}

resource "aws_security_group" "cidr_ipv6" {
  ingress {
    protocol         = -1
    ipv6_cidr_blocks      = ["::/0"] # Noncompliant
  }
}

resource "aws_security_group" "noncompliant_allow_inbound_ssh" {
  ingress {
    from_port        = 22
    #                  ^^> {{Port range start}}
    to_port          = 22
    #                  ^^> {{Port range end}}
    protocol         = tcp
    #                  ^^^> {{Related protocol setting}}
    cidr_blocks      = ["0.0.0.0/0"] # Noncompliant
    #                  ^^^^^^^^^^^^^
  }
}

resource "aws_security_group" "port_range_contains_rdp" {
  ingress {
    from_port        = 3000
    to_port          = 4000
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"] # Noncompliant
  }
}

resource "aws_security_group" "ports_are_zero" {
  ingress {
    from_port        = 0
    to_port          = 0
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"] # Noncompliant
  }
}

resource "aws_security_group" "ports_zero_to_ssh" {
  ingress {
    from_port        = 0
    to_port          = 22
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"] # Noncompliant
  }
}

resource "aws_security_group" "not_tcp_port" {
  ingress {
    from_port        = 22
    to_port          = 22
    protocol         = udp
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "no_default_routing_cidr" {
  ingress {
    protocol         = -1
    cidr_blocks      = ["192.168.0.0/23"]
  }
}

resource "aws_security_group" "no_default_routing_cidrv6" {
  ingress {
    protocol         = -1
    ipv6_cidr_blocks      = ["2002::1234:abcd:ffff:c0a8:101/64"]
  }
}

resource "aws_security_group" "missing_protocol" {
  ingress {
    from_port        = 0
    to_port          = 0
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "missing_from_port" {
  ingress {
    protocol         = tcp
    to_port          = 0
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "missing_to_port" {
  ingress {
    protocol         = tcp
    from_port          = 0
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_not_security_group" "not_a_security_group" {
  ingress {
    protocol         = -1
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "invalid_protocol_int" {
  ingress {
    protocol         = !1
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "invalid_protocol_int_2" {
  ingress {
    protocol         = -2
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "port_range_contains_no_rdp_and_no_ssh" {
  ingress {
    from_port        = 9000
    to_port          = 10000
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "invalid_from_port" {
  ingress {
    from_port        = invalid
    to_port          = 0
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "invalid_to_port" {
  ingress {
    from_port        = 0
    to_port          = invalid
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "invalid_to_port" {
  ingress {
    from_port        = invalid
    to_port          = invalid
    protocol         = tcp
    cidr_blocks      = ["0.0.0.0/0"]
  }
}
