FROM scratch

## Dev environment
ARG env_string=env
ARG other=other
ARG dev_env=dev
ARG prd_env=prd

# Sensitive
# Noncompliant@+1 {{Do not enable debugging features on production servers.}}
  ENV env=dev
#     ^^^^^^^
# Noncompliant@+1
  ENV another=var env=dev
#                 ^^^^^^^
# Noncompliant@+1
ENV env dev
# Noncompliant@+1
ENV ENV=DEV
# Noncompliant@+1
ENV environment=dev
# Noncompliant@+1
ENV server_env=dev
# Noncompliant@+1
ENV _env=dev
# Noncompliant@+1
ENV serverenv=dev
# Noncompliant@+1
ENV envenv=dev
# Noncompliant@+1
ENV env=devel
# Noncompliant@+1
ENV env=develop
# Noncompliant@+1
ENV env=development
# Noncompliant@+1
ENV env='dev'
# Noncompliant@+1
ENV env="dev"
# Noncompliant@+1
ENV env=$dev_env
# Noncompliant@+1
ENV $env_string=dev

# Compliant
ENV env=devmode
ENV env dev mode
ENV envir=dev
ENV env=develo
ENV env=val var=dev
ENV dev_env=true
ENV server6_env=dev
ENV env=$prd_env
ENV env=$unknown_env
ENV $other=dev
ENV $unknown=dev


## Debug mode
ARG enabled=true
ARG disabled=false
ARG debug_string=debug
ARG other=other

# Sensitive
# Noncompliant@+1 {{Do not enable debugging features on production servers.}}
  ENV debug=true
#     ^^^^^^^^^^
# Noncompliant@+1
  ENV var=val debug=true
#             ^^^^^^^^^^
# Noncompliant@+1
ENV DEBUG true
# Noncompliant@+1
ENV DEBUG=true
# Noncompliant@+1
ENV debug=TRUE
# Noncompliant@+1
ENV debug=yes
# Noncompliant@+1
ENV debug=on
# Noncompliant@+1
ENV debug=1
# Noncompliant@+1
ENV mode_debug=true
# Noncompliant@+1
ENV anydebug=true
# Noncompliant@+1
ENV _debug=true
# Noncompliant@+1
ENV debug=$enabled
# Noncompliant@+1
ENV $debug_string=true

# Compliant
ENV debug=0
ENV DEBUG true 1
ENV debug=false
ENV debug=no
ENV debug_mode=true
ENV debug_enabled=true
ENV mode6_debug=true
ENV debug=$disabled
ENV debug=$unknown
ENV debug=val var=false


## PHPX debug mode
ARG xdebug_mode_string=xdebug_mode
ARG not_xdebug_mode=random
ARG php_enabled=ON
ARG php_disabled=OFF

# Sensitive
# Noncompliant@+1 {{Do not enable debugging features on production servers.}}
  ENV xdebug_mode=on
#     ^^^^^^^^^^^^^^
# Noncompliant@+1
  ENV var=val xdebug_mode=on
#             ^^^^^^^^^^^^^^
# Noncompliant@+1
ENV xdebug_mode=ON
# Noncompliant@+1
ENV xdebug_mode ON
# Noncompliant@+1
ENV xdebug_mode off off
# Noncompliant@+1
ENV XDEBUG_MODE=on
# Noncompliant@+1
ENV php_xdebug_mode=on
# Noncompliant@+1
ENV phpxdebug_mode=on
# Noncompliant@+1
ENV xdebug_mode=true
# Noncompliant@+1
ENV xdebug_mode=1
# Noncompliant@+1
ENV xdebug_mode=anything
# Noncompliant@+1
ENV xdebug_mode=$php_enabled
# Noncompliant@+1
ENV $xdebug_mode_string=1

# Compliant
ENV xdebug_mode=off
ENV xdebug_mode off
ENV notx_debug_mode=on
ENV phpxdebug_mode=off
ENV php_xdebug_mode=off
ENV xdebug_mode=$php_disabled
ENV $not_xdebug_mode=off
