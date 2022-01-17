SUMMARY = "RETIS Lab devicetree tweaks"
LICENSE = "MIT"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://system-user.dtsi"

python () {
    if d.getVar("CONFIG_DISABLE"):
        d.setVarFlag("do_configure", "noexec", "1")
}

export PETALINUX
do_configure_append () {
	script="${PETALINUX}/etc/hsm/scripts/petalinux_hsm_bridge.tcl"
	data=${PETALINUX}/etc/hsm/data/
	eval xsct -sdx -nodisp ${script} -c ${WORKDIR}/config \
	-hdf ${DT_FILES_PATH}/hardware_description.${HDF_EXT} -repo ${S} \
	-data ${data} -sw ${DT_FILES_PATH} -o ${DT_FILES_PATH} -a "soc_mapping"
}


# refer to the following links for info related to device-tree:
# https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/plain/Documentation/devicetree/overlay-notes.rst
# https://elinux.org/Device_Tree_Reference

# DT Example 1) from original doc
#	/dts-v1/;
#	/plugin/;
#	&ocp {
#		/* bar peripheral */
#		bar {
#			compatible = "corp,bar";
#			... /* various properties and child nodes */
#		};
#	};

# DT Example 2) FRED
#/include/ "system-conf.dtsi"
#/ {
#    amba {
#        #address-cells = <1>;
#        #size-cells = <1>;
#        slot_p0_s0@A0000000 { 
#            compatible = "generic-uio";
#            reg = <0xA0000000 0x10000>;
#            interrupt-parent = <0x4>;
#            interrupts = <0x0 0x1d 0x4>; 
#            }; 
#
#        pr_decoupler_p0_s0@A0010000 { 
#            compatible = "generic-uio";
#            reg = <0xA0010000 0x10000>;
#            }; 
#    };
#};


