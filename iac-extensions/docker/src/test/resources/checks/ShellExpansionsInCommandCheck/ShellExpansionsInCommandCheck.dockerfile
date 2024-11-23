FROM scratch

# Noncompliant@+1 {{Prefix files and paths with ./ or -- when using glob.}}
RUN rm *
#      ^
# Noncompliant@+1
RUN rm *.txt
#      ^^^^^
# Noncompliant@+1
RUN rm *g
# Noncompliant@+1
RUN rm * --
# Noncompliant@+1
RUN echo *
# Noncompliant@+1
RUN echo -n *
# Noncompliant@+1
RUN printf *.txt
# Noncompliant@+1
RUN grep text_pattern *.txt
# Noncompliant@+1
RUN touch -- '-f oo.bar' && ls *.bar
#                              ^^^^^
# Noncompliant@+1
RUN *
# Noncompliant@+1
RUN rm "--" *
# Noncompliant@+1
RUN rm '--' *

# Compliant
RUN rm "*" && rm '*' && rm "*.txt"
RUN rm ./*
RUN rm -- *
RUN rm file.*
RUN echo 'Files: ' *
RUN printf '%s ' *
RUN for i in *.gz; do tar zxvf $i; done;
RUN (cd ${LIBSYSTEMD}/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done);
# Compliant: asterisk in ExecForm won't be subject for shell expansion
CMD [ "/usr/bin/daemon", "--allowed", "*"]
RUN echo "* * * * * umask 007; $APP_ROOT_PATH/bin/magento"
# Compliant: find command is excluded from the check
RUN find . -name *.log -exec rm {} \;

# Noncompliant@+3
RUN case "${alpineArch##*-}" in \
      x86_64) \
        rm * ;; \
      x86_64) \
        echo 'Files: ' * ;; \
      *32) ;; \
      # FN as we don't analyze deeper commands separately
      *32) echo $(ls *) ;; \
      *) ;; \
      *) echo Default ;; \
      *)echo Default ;; \
    esac

# Noncompliant@+1
ENTRYPOINT echo *
# Noncompliant@+1
CMD printf *

RUN <<EOT
  find . -name *.log -exec rm {} \;
EOT

RUN <<EOT
  rm *
EOT
