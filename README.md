# Arbitrary Precision Arithmetic Library (CS1023)

Tiny Java library that supports **arbitrary-precision integers and floating-point numbers**.  
Includes a CLI driver for one-off calculations and an Ant build script for easy automation.

## Quick Start

### 1 · Run with Docker  
```bash
docker pull mercurialus/inf-cal
docker run -it mercurialus/inf-cal int add 2 3   # → 5
```

### 2 · Build Locally  
```bash
# prerequisites: JDK 17+ and Apache Ant
git clone https://github.com/mercurialus/infinite-precision-calculator-java
cd infinite-precision-calculator-java
python3 run_project.py clean | build | <int|float> <add|sub|mul|div> <op1> <op2>
#to get a jar file, run
ant jar
```

## Project Layout
```plain
src/
├── arbitraryarithmetic/
    ├── AInteger.java       # big-int core
    ├── AFloat.java         # big-float core
├── MyInfArith.java         # CLI entry point
├── build.xml               # Ant tasks: clean · compile · jar · run
├── dockerfile 
└── run_project.py          #python wrapper to run the code 
```
