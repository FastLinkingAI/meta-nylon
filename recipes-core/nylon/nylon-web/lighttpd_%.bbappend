# Append to lighttpd recipe to enable PHP support

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Add our custom lighttpd.conf to source files
SRC_URI += "file://lighttpd.conf"

# Install our custom lighttpd.conf with PHP support
do_install:append() {
    install -m 0644 ${WORKDIR}/lighttpd.conf ${D}${sysconfdir}/lighttpd/lighttpd.conf
}

# Add dependency on PHP CGI
RDEPENDS:${PN} += "php-cgi"