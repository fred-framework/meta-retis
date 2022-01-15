SUMMARY = "RETIS Lab kernel development image"
LICENSE = "MIT"
# This image provides wifi, ethernet, and usual development tools, including kernel level development

inherit core-image-kernel-dev
require retis-dev-image.bb

# might be usefull to include
IMAGE_INSTALL_append = " \
    kernel-dev  \
    kernel-devsrc \
    cyclitest \
    lmbench \
    stress-ng \
    sysbench \    
"
