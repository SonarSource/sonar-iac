FROM scratch

# FN: shell form not supported in community edition
RUN rm *

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "rm *.txt"]
RUN ["sh", "-c", "rm *g"]
RUN ["sh", "-c", "rm * --"]
RUN ["sh", "-c", "echo *"]
RUN ["sh", "-c", "echo -n *"]
RUN ["sh", "-c", "printf *.txt"]
RUN ["sh", "-c", "grep text_pattern *.txt"]
RUN ["sh", "-c", "touch -- '-f oo.bar' && ls *.bar"]
RUN ["sh", "-c", "*"]
RUN ["sh", "-c", "rm \"--\" *"]
RUN ["sh", "-c", "rm '--' *"]

# Compliant
RUN ["sh", "-c", "rm \"*\" && rm '*' && rm \"*.txt\""]
RUN ["sh", "-c", "rm ./*"]
RUN ["sh", "-c", "rm -- *"]
RUN ["sh", "-c", "rm file.*"]
RUN ["sh", "-c", "echo 'Files: ' *"]
RUN ["sh", "-c", "printf '%s ' *"]
RUN ["sh", "-c", "for i in *.gz; do tar zxvf $i; done;"]
RUN ["sh", "-c", "(cd ${LIBSYSTEMD}/sysinit.target.wants/; for i in *; do [ $i == systemd-tmpfiles-setup.service ] || rm -f $i; done);"]
# Compliant: asterisk in ExecForm won't be subject for shell expansion
CMD [ "/usr/bin/daemon", "--allowed", "*"]
RUN ["sh", "-c", "echo \"* * * * * umask 007; $APP_ROOT_PATH/bin/magento\""]
# Compliant: find command is excluded from the check
RUN ["sh", "-c", "find . -name *.log -exec rm {} \\;"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "case \"${alpineArch##*-}\" in x86_64) rm * ;; x86_64) echo 'Files: ' * ;; *32) ;; *32) echo $(ls *) ;; *) ;; *) echo Default ;; *)echo Default ;; esac"]

ENTRYPOINT echo *

CMD ["sh", "-c", "printf *"]

RUN <<EOT
  find . -name *.log -exec rm {} \;
EOT

RUN <<EOT
  rm *
EOT

