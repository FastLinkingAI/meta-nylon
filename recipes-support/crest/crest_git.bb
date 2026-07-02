SUMMARY = "cREST RESTful framework for C (FastCGI)"
DESCRIPTION = "Lightweight C REST framework used by NylonWeb, built on cJSON and FastCGI."
HOMEPAGE = "https://github.com/JeffTsengTwn/cREST"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYING;md5=c9900ea6c362454ca1ba5b9fcd88fea4"

DEPENDS = "cjson fcgi"

SRC_URI = "git://github.com/JeffTsengTwn/cREST.git;protocol=https;branch=main \
           file://COPYING \
           "
# Pinned to the revision referenced by NylonWeb's packges/cREST submodule.
SRCREV = "04c24867145a987bca301fc0f4ffad81b560c087"

S = "${WORKDIR}/git"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit pkgconfig

do_compile() {
	${CC} ${CFLAGS} -fPIC -shared -I${S} -I${STAGING_INCDIR}/cjson \
		${S}/cREST.c ${LDFLAGS} -lcjson \
		-o ${B}/libcrest.so
}

do_install() {
	install -d ${D}${libdir} ${D}${includedir}
	install -m 0755 ${B}/libcrest.so ${D}${libdir}/libcrest.so
	ln -sf libcrest.so ${D}${libdir}/libcrest.so.0
	install -m 0644 cREST.h ${D}${includedir}/cREST.h
}

FILES:${PN} += "${libdir}/libcrest.so.0"
