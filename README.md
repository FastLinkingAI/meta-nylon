# meta-nylon

This is the first-party Yocto/OpenEmbedded layer for **NylonOS**, the OpenWRT-flavored firmware built on top of Poky for the Raspberry Pi. It provides the recipes and configuration needed to install and serve **NylonWeb** — the router's web management UI — on the target image.

## Layer dependencies

| Layer                  | Branch      |
| ----------------------- | ----------- |
| `openembedded-core`    | `kirkstone` |
| `meta-openwrt`         | `kirkstone` |

- `LAYERSERIES_COMPAT_meta-nylon = "kirkstone"`
- `BBFILE_PRIORITY_meta-nylon = "6"`

## Directory structure

```
meta-nylon/
├── conf/
│   └── layer.conf                          # layer definition
├── recipes-core/
│   ├── images/
│   │   └── openwrt-image-minimal.bbappend  # installs lighttpd + nylon-web into the image
│   └── nylon/
│       └── nylon-web/
│           ├── nylon-web_git.bb            # NylonWeb SPA + FastCGI backend recipe
│           ├── lighttpd_%.bbappend         # lighttpd config/module wiring for NylonWeb
│           └── files/
│               ├── lighttpd.conf           # FastCGI + HTTPS static SPA config
│               ├── users                   # default UCI user config
│               └── COPYING
└── recipes-support/
    └── crest/
        ├── crest_git.bb                    # cREST C RESTful framework recipe
        └── files/COPYING
```

## Recipes

### `nylon-web` (`recipes-core/nylon/nylon-web`)

Builds [NylonWeb](https://github.com/FastLinkingAI/NylonWeb), the router management UI:

- **Frontend**: Vue 3 SPA, built with `npm ci && npm run build` and installed to `/var/www/public`.
- **Backend**: a FastCGI REST service (`nylonweb_cgi`) compiled from the `cgi/` sources, linked against `cREST`, `cJSON`, `libubox`, `uci`, and `fcgi`, exposing UCI-based configuration (device, firewall, network, status, system, user, wifi controllers).
- Installs a default UCI user config at `${sysconfdir}/config/users` (default credentials `admin` / `admin123` — **change these before shipping/production use**).

### `lighttpd` (`recipes-core/nylon/nylon-web/lighttpd_%.bbappend`)

Bbappend on top of the upstream `lighttpd` recipe to serve NylonWeb:

- Installs a custom `lighttpd.conf` that serves the built SPA as static files and proxies `/rest/*` to `nylonweb_cgi` over FastCGI (unix socket).
- Enables HTTPS on port 443 with a self-signed certificate generated at build time (`do_install:append`), plus the `fastcgi`, `openssl`, `access`, `indexfile`, and `accesslog` lighttpd modules.
- Depends on `openssl-native` and `RDEPENDS` on `nylon-web`.

### `crest` (`recipes-support/crest`)

Packages [cREST](https://github.com/JeffTsengTwn/cREST), the lightweight C REST framework (built on `cJSON` and FastCGI) that `nylon-web`'s backend links against. Built as `libcrest.so`.

### `openwrt-image-minimal.bbappend` (`recipes-core/images`)

Appends `lighttpd` and `nylon-web` to `IMAGE_INSTALL` so they're included in the final image.

## Adding the meta-nylon layer to your build

```bash
bitbake-layers add-layer meta-nylon
```

(Already configured in this repo's `build-rpi/conf/bblayers.conf`.)

## Notes

- After changing a recipe's source files, run `bitbake -c cleansstate <recipe>` (or `-c clean`) before rebuilding for the change to take effect.
- The default `admin/admin123` credentials in `nylon-web`'s `files/users` are intended for development only.
