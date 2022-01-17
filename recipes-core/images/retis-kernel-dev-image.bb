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

# This examples shows how to replace the version of a package, in this case stress-ng.
# By looking at this link https://layers.openembedded.org/layerindex/recipe/104982/, 
# we see that Yocto Zeus uses stress-ng 0.10.00
# https://layers.openembedded.org/layerindex/recipe/107342/
# But we want to update it to stress-ng 0.13.09
# http://cgit.openembedded.org/openembedded-core/plain/meta/recipes-extended/stress-ng/stress-ng_0.13.09.bb
# In this situation, we need to say the following command to select the version to be deployed
PREFERRED_VERSION_stress-ng = "0.13.09"
# Moreover, the recipe related to the new package is included in recipes-apps/real-time/stress-ng_0.13.09.bb.

# uncomment to install additional tracers
# https://xilinx-wiki.atlassian.net/wiki/spaces/A/pages/123011167/Linux+Debug+infrastructure+KProbe+UProbe+LTTng
# IMAGE_INSTALL_append = " lttng-tools lttng-modules lttng-ust" 
