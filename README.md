# meta-retis

Yocto Image from [Real-Time Systems Laboratory (ReTiS Lab)](https://retis.santannapisa.it/), [Scuola Superiore Sant'Anna (SSSA)](https://www.santannapisa.it/), Pisa, Italy.

## Creating a PetaLinux Project

Create a PetaLinux project using the following command:
```
petalinux-create -t project -s <path to the xilinx-zcu102-v2020.2-final.bsp>
```
Reconfigure the project with the hardware design (.xsa or .bsp):
```
cd xilinx-zcu102-2020.2
petalinux-config --get-hw-description=<path containing edt_zcu102_wrapper.xsa>
```
In this case, one can use either the [default project](https://xilinx.github.io/Embedded-Design-Tutorials/docs/2020.2/docs/Introduction/ZynqMPSoC-EDT/3-system-configuration.html) or a custom vivado design. 


## Image Customization

### Kernel Customization

To be done !

```
petalinux-config -c kernel
```

```
source components/yocto/layers/core/oe-init-build-env 
bitbake-layers show-appends
bitbake-layers show-appends | grep retis
...
linux-xlnx_2020.2.bb:
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/yocto/layers/meta-xilinx/meta-xilinx-contrib/recipes-kernel/linux/linux-xlnx_2020.2.bbappend
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/yocto/layers/meta-petalinux/recipes-kernel/linux-xlnx/linux-xlnx_%.bbappend
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/project-spec/meta-user/recipes-kernel/linux/linux-xlnx_%.bbappend
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/ext_source/meta-retis/recipes-kernel/linux/linux-xlnx_2020.2.bbappend
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/yocto/workspace/appends/linux-xlnx_2020.2.bbappend
...
```

components/yocto/workspace/sources/linux-xlnx/oe-local-files/

The kernel configuration fragments are located here:
./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/cpu_idle.cfg

The resulting kernel configuration file can be found here:

./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/linux-xlnx-5.4+git999/.config


The log files related to kernel compilation are located here
./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/temp/

```
petalinux-build  -c <image-name> -x compile
```

### Updating the Device tree

https://www.centennialsoftwaresolutions.com/post/using-device-tree-overlays-with-petalinux

Passing the -@ flat to dtc will make it retain information about labels when generating a dtb file, which will allow Linux to figure out at runtime what device tree node a label was referring to.

CONFIG_SUBSYSTEM_DEVICETREE_COMPILER_FLAGS="-@" 

### PREEMPT_RT Kernel Support

To be done !

### RootFS Customization

To be done !

```
petalinux-config -c rootfs
```


## Image Building and Packing


In the `<PetaLinux-project>` directory, build the Linux images using the following command:
```
$ petalinux-build
```
This last command builds the image called `petalinux-image-minimal`, defined in `./components/yocto/layers/meta-petalinux/recipes-core/images/petalinux-image-minimal.bb`. Another alternative image is called `petalinux-image-full`. To choose it, run:

```
$ petalinux-build -c petalinux-image-full
```
This last command takes a long time when it is first executed. After the build process is finished, verify the images in the directory `images/linux`. 

Go to the image directory and generate the boot image `BOOT.BIN` using the following command:

```
cd images/linux
petalinux-package --boot --fsbl zynqmp_fsbl.elf --u-boot
```

Or executing :

```
cd images/linux
petalinux-package --boot --fsbl zynqmp_fsbl.elf --fpga system.bit --pmufw pmufw.elf --atf bl31.elf --u-boot u-boot.elf
```
If the vivado design uses the **PL** part. Note that this last option includes the .bit into the `BOOT.BIN`. 

## Image Deploy

Copy the `BOOT.BIN`, `image.ub`, and `boot.scr` files to the **boot partition** of the SD card, formated with **FAT32**. 
Copy the `rootfs` file to the **rootfs partition** of the SD card, formated with **EXT4**. 

## TODO

 - [ ] Supporting PREEMP_RT;
 - [ ] Test the images;
 - [ ] Create images for Xilinx support;
 - [ ] 

## Contributions

  Did you find a bug in this tutorial ? Do you have some extensions or updates to add ? Please send us a Pull Request.

## Authors

 - Alexandre Amory (December 2021), ReTiS Lab, Scuola Sant'Anna, Pisa, Italy.

## Funding
 
This software package has been developed in the context of the [AMPERE project](https://ampere-euproject.eu/). This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 871669.
