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

```
$ petalinux-config
$ cd build
$ ln -s <yocto-download-dir> downloads
$ ln -s <yocto-sstate-cache> sstate-cache
```

## Including `meta-retis` Layer

```
$ mkdir components/ext_source
$ cd components/ext_source
$ git clone -b petalinux https://github.com/fred-framework/meta-retis.git
$ cd meta-retis
$ tree
.
├── conf
│   └── layer.conf
├── LICENSE
├── README.md
├── recipes-core
│   ├── images
│   │   ├── retis-dev-image.bb
│   │   ├── retis-image.bb
│   │   └── retis-kernel-dev-image.bb
│   ├── packagegroups
│   │   └── retis-packagegroup-testing.bb
└── recipes-kernel
    └── linux
        └── linux-xlnx
            ├── files
            │   ├── defconfig
            │   ├── ftrace.cfg
            │   └── rt-frag.cfg
            └── linux-xlnx_2020.2.bbappend
```


```
$ cd build
$ petalinux-config
  Yocto Settings  --->  User Layers  ---> 
    ${PROOT}/components/ext_source/meta-retis
```

## Image Customization

This layer extends the default petalinux image called `petalinux-image-minimal`, creating three new images:

 - [`retis-image`](./recipes-core/images/retis-image.bb): Includes the `petalinux-image-minimal` plus editors and htop. Access via serial interface or keyboard/hdmi;
 - [`retis-dev-image`](./recipes-core/images/retis-dev-image.bb): Includes the previous image,  ethernet/wifi, and conventional development tools, e.g., gcc, make, etc. Access via SSH; 
 - [`retis-kernel-dev-image`](./recipes-core/images/retis-dev-image.bb): Includes the previous image, and kernel development tools and tracers;

### Kernel Customization

The kernel customization can be done manually by executing this command:

```
$ petalinux-config -c kernel
```

Or, preferably, it can be done automatically using the bbappend of this layer. For this situation, run the following command to check whether the kernel bbappend file was detected:

```
$ cd <proj-dir>
$ source components/yocto/layers/core/oe-init-build-env 
$ bitbake-layers show-appends | grep retis
...
linux-xlnx_2020.2.bb:
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/ext_source/meta-retis/recipes-kernel/linux/linux-xlnx_2020.2.bbappend
...
```

If the bbappend is listed, then we have a successful initial configuration. Next, we run the kernel compilation, using one of the images mentioned before:

```
$ petalinux-build  -c <image-name> -x compile
```

All the kernel configuration files will be stores in the directory `./components/yocto/workspace/sources/linux-xlnx/oe-local-files/`

The kernel configuration fragments can also be located here `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/cpu_idle.cfg` and the final configuration file can be found here `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/linux-xlnx-5.4+git999/.config`. Check wheter the final configuration file is correct by checking the expected option values. For instance:

```
$ cat ./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/linux-xlnx-5.4+git999/.config | grep CONFIG_HZ_1000
```

The expected value is `y`. In case of error during the kernel compilation, please check the logs in the directory `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git999-r0/temp/`.

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
