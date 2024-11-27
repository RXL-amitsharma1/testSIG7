from setuptools import setup
from Cython.Distutils import build_ext

from distutils.extension import Extension
ext_modules=[ 
    Extension("ror_prr", ["ror_prr.py"]),
    ]

for e in ext_modules:
    e.cython_directives = {'language_level': "3"}

setup(name = "ror_prr",
version ="1",
description= "This package is for transforming subgroup data received from Database as per PVS application requirement",
long_description="This package is for transforming subgroup data received from Database as per PVS application requirement",
author="prateek dagar" ,
cmdclass = {"build_ext" :build_ext} ,
ext_modules = ext_modules
)
