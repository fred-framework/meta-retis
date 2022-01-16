SUMMARY = "RETIS Lab kernel development image"
LICENSE = "MIT"
# This image provides wifi, ethernet, and usual development tools, including kernel level development

inherit core-image
require retis-dev-image.bb

# might be usefull to include
#  - rt-tests : https://wiki.linuxfoundation.org/realtime/documentation/howto/tools/rt-tests#compile-and-install
#    includes ciclictest among others
IMAGE_INSTALL_append = " \
    hwlatdetect \
    kernel-dev  \
    kernel-devsrc \
    lmbench \
    rt-tests \
    stress-ng \
    sysbench \    
"
