SUMMARY = "NylonWeb router management UI"
DESCRIPTION = "Vue 3 SPA with FastCGI REST backend (nylonweb_cgi) for OpenWrt-style UCI configuration."
HOMEPAGE = "https://github.com/FastLinkingAI/NylonWeb"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYING;md5=15e08d4dfaf5f42d190f3fd467ebbbc0"

DEPENDS = "cjson crest fcgi uci libubox json-c nodejs-native"

RDEPENDS:${PN} = "cjson crest fcgi uci libubox"

SRC_URI = "git://github.com/FastLinkingAI/NylonWeb.git;protocol=https;branch=main \
           file://users \
           file://COPYING \
           "
SRCREV = "1ef0a6e93d41edadbf42c02c53cf82de392dfe81"

S = "${WORKDIR}/git"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

inherit pkgconfig

# npm needs to reach the registry to install webui/node_modules; bitbake
# isolates tasks from the network by default (network namespace), so this
# task must opt back in explicitly.
do_compile[network] = "1"

# Upstream's top-level Makefile recurses into vendored git submodules
# (packges/cJSON, packges/cREST) for its own "clean"/"all" targets, but we
# fetch with a plain git:// URL (no submodule content) and build against
# the standalone cjson/crest recipes via DEPENDS instead. Skip the default
# base_do_configure (oe_runmake clean), which would otherwise fail trying
# to recurse into those empty submodule directories.
do_configure[noexec] = "1"

CGI_SRCS = "DeviceController FirewallController NetworkController StatusController \
            SystemController UserController WifiController UciUtil main"

do_compile() {
	# Vue 3 frontend
	cd ${S}/webui
	npm ci --no-audit --no-fund
	npm run build

	# FastCGI backend
	mkdir -p ${B}/cgi/obj
	for src in ${CGI_SRCS}; do
		${CC} ${CFLAGS} -Wall -I${S}/cgi/include -I${STAGING_INCDIR}/cjson \
			-c -o ${B}/cgi/obj/${src}.o ${S}/cgi/src/${src}.c
	done
	${CC} ${LDFLAGS} -o ${B}/nylonweb_cgi ${B}/cgi/obj/*.o \
		-lcrest -lcjson -luci -lubox -lfcgi
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 ${B}/nylonweb_cgi ${D}${bindir}/nylonweb_cgi

	install -d ${D}/var/www/public
	cp -r ${S}/webui/dist/. ${D}/var/www/public/

	install -d ${D}${sysconfdir}/config
	install -m 0644 ${WORKDIR}/users ${D}${sysconfdir}/config/users
}

FILES:${PN} += "/var/www/public"
CONFFILES:${PN} += "${sysconfdir}/config/users"
