SUMMARY = "RETIS Lab development image"
LICENSE = "MIT"
# This image provides wifi, ethernet, and usual development tools

inherit core-image
require retis-image.bb
require ./recipes-core/images/petalinux-image-minimal.bb
#require ./recipes-core/images/petalinux-image-full.bb

#
# Extra image configuration defaults
#
# The EXTRA_IMAGE_FEATURES variable allows extra packages to be added to the generated
# images. Some of these options are added to certain image types automatically. The
# variable can contain the following options:
#  "dbg-pkgs"       - add -dbg packages for all installed packages
#                     (adds symbol information for debugging/profiling)
#  "src-pkgs"       - add -src packages for all installed packages
#                     (adds source code for debugging)
#  "dev-pkgs"       - add -dev packages for all installed packages
#                     (useful if you want to develop against libs in the image)
#  "ptest-pkgs"     - add -ptest packages for all ptest-enabled packages
#                     (useful if you want to run the package test suites)
#  "tools-sdk"      - add development tools (gcc, make, pkgconfig etc.)
#  "tools-debug"    - add debugging tools (gdb, strace)
#  "eclipse-debug"  - add Eclipse remote debugging support
#  "tools-profile"  - add profiling tools (oprofile, lttng, valgrind)
#  "tools-testapps" - add useful testing tools (ts_print, aplay, arecord etc.)
#  "debug-tweaks"   - make an image suitable for development
#                     e.g. ssh root access has a blank password
# There are other application targets that can be used here too, see
# meta/classes/image.bbclass and meta/classes/core-image.bbclass for more details.
# We default to enabling the debugging tweaks.
EXTRA_IMAGE_FEATURES ?= "debug-tweaks"

#
# Additional image features
#
# The following is a list of additional classes to use when building images which
# enable extra features. Some available options which can be included in this variable
# are:
#   - 'buildstats' collect build statistics
#   - 'image-mklibs' to reduce shared library files size for an image
#   - 'image-prelink' in order to prelink the filesystem image
# NOTE: if listing mklibs & prelink both, then make sure mklibs is before prelink
# NOTE: mklibs also needs to be explicitly enabled for a given image, see local.conf.extended
USER_CLASSES ?= "buildstats image-mklibs image-prelink"

# Supported values are auto, gnome, xfce, rxvt, screen, konsole (KDE 3.x only), none
# Note: currently, Konsole support only works for KDE 3.x due to the way
# newer Konsole versions behave
#OE_TERMINAL = "auto"
# By default disable interactive patch resolution (tasks will just fail instead):
PATCHRESOLVE = "noop"

# wifi connectivity features
DISTRO_FEATURES_append = " wifi"
# this packagroup includes:
#"packagegroup-base-wifi" -> "iw"
#"packagegroup-base-wifi" -> "kernel-module-aes-generic" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-aes" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-arc4" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-crypto_algapi" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-cryptomgr" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-ecb" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-ieee80211-crypt-ccmp" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-ieee80211-crypt" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-ieee80211-crypt-tkip" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-ieee80211-crypt-wep" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-michael-mic" [style=dotted]
#"packagegroup-base-wifi" -> "kernel-module-zd1211rw" [style=dotted]
#"packagegroup-base-wifi" -> "wireless-regdb-static"
#"packagegroup-base-wifi" -> "wpa-supplicant"
DISTRO_FEATURES_append = " usbhost"
# this packagroup includes:
#"packagegroup-base-usbhost" -> "kernel-module-ehci-hcd" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-mousedev" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-ohci-hcd" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-scsi-mod" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-sd-mod" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-uhci-hcd" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usbcore" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usbhid" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usbmouse" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usbnet" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usbserial" [style=dotted]
#"packagegroup-base-usbhost" -> "kernel-module-usb-storage" [style=dotted]
#"packagegroup-base-usbhost" -> "usbutils"
DISTRO_FEATURES_append = " usbgadget"
# this packagroup includes:

# network extra tools
IMAGE_INSTALL_append = " \
        bridge-utils \
        dhcp-client \
        iptables"

# extra dev tools
IMAGE_INSTALL_append = " \
    bash \
	bash-completion \
    coreutils \
    cmake \
    dtc \
    file \
    git \
    gzip \
    less \
    ldd \
    openssh \
    perf \
    wget \
"

# buildessential installs: gcc, make, autoconf, etc
CORE_IMAGE_EXTRA_INSTALL += " \
    retis-packagegroup-testing \
    packagegroup-core-buildessential \
"

# other features that might be interesting, defined in :
# /opt/yocto/dunfell/src/poky/meta/recipes-rt/images/core-image-rt-sdk.bb
# IMAGE_FEATURES += "dev-pkgs tools-sdk tools-debug eclipse-debug tools-profile tools-testapps debug-tweaks"
# IMAGE_INSTALL += "rt-tests hwlatdetect kernel-dev"

# kernel dev tools
#IMAGE_INSTALL_append = " \
#    kernel-dev  \
#    kernel-devsrc \
#    cyclitest \
#    lmbench \
#    stress-ng \
#    sysbench \    
#"

# install the package-management tool (opkg, dnf, apt), depending on the format selected next. the default is rpm.
EXTRA_IMAGE_FEATURES += " package-management " 
# this is the address to the package repository
PACKAGE_FEED_URIS = "http://10.30.3.59:8000" 

# features to be included
IMAGE_FEATURES += "ssh-server-openssh tools-debug debug-tweaks"
IMAGE_FEATURES_remove += "ssh-server-dropbear"

#CORE_IMAGE_EXTRA_INSTALL += "libgomp libgomp-dev libgomp-staticdev"

# defined in components/yocto/layers/core/meta/recipes-core/packagegroups/packagegroup-core-sdk.bb
#RDEPENDS_${PN} = " \ 
#" 

###########################
# VNC
# TODO: not tested yet
###########################
# VNC has lots of depedency. It's heavy. Be sure you need it.
#IMAGE_INSTALL_append += " x11vnc"

# set the free space in the image. If not set, it will be minimal, i.e. few hundreds of MBytes
#IMAGE_ROOTFS_EXTRA_SPACE_append = " + 8000000"

## check other petalinux packagegroups that can be helpfull in 
# ./components/yocto/layers/meta-petalinux/recipes-core/images/petalinux-image-full.inc