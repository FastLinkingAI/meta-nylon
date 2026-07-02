# NylonWeb lighttpd configuration (FastCGI + static Vue SPA)

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://lighttpd.conf"

DEPENDS:append = " openssl-native"

RDEPENDS:${PN} += " \
    lighttpd-module-fastcgi \
    lighttpd-module-openssl \
    lighttpd-module-access \
    lighttpd-module-indexfile \
    lighttpd-module-accesslog \
    nylon-web \
"

do_install:append() {
	install -m 0644 ${WORKDIR}/lighttpd.conf ${D}${sysconfdir}/lighttpd/lighttpd.conf

	install -d ${D}${sysconfdir}/lighttpd/ssl
	${STAGING_BINDIR_NATIVE}/openssl req -x509 -nodes -days 3650 -newkey rsa:2048 \
		-keyout ${WORKDIR}/server.key \
		-out ${WORKDIR}/server.crt \
		-subj "/CN=nylonweb/O=NylonOS/C=TW"
	cat ${WORKDIR}/server.key ${WORKDIR}/server.crt > ${D}${sysconfdir}/lighttpd/ssl/server.pem
	chmod 0600 ${D}${sysconfdir}/lighttpd/ssl/server.pem
}
