SUMMARY = "RETIS Lab kernel development image"
LICENSE = "MIT"
# This image provides wifi, ethernet, and usual development tools, including kernel level development

inherit core-image-kernel-dev
require retis-dev-image.bb

# This recipe tracks the 'bleeding edge' linux-xlnx repository.
# Since this tree is frequently updated, AUTOREV is used to track its contents.
#
# To enable this recipe, set PREFERRED_PROVIDER_virtual/kernel = "linux-xlnx-dev"
# ./components/yocto/layers/meta-xilinx/meta-xilinx-bsp/recipes-kernel/linux/linux-xlnx-dev.bb

# might be usefull to include
#  - rt-tests : https://wiki.linuxfoundation.org/realtime/documentation/howto/tools/rt-tests#compile-and-install
#    includes ciclictest among others
IMAGE_INSTALL_append = " \
    kernel-dev  \
    kernel-devsrc \
    lmbench \
    rt-tests \
    stress-ng \
    sysbench \    
"
