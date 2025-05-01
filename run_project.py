import os
import subprocess
import sys

def is_valid_number(value, dtype):
    try:
        if dtype == 'int':
            return str(int(value)) == value
        elif dtype == 'float':
            return True
        else:
            return False
    except ValueError:
        return False

def main():
    build_file = os.path.join(os.path.dirname(__file__), "build.xml")

    if not os.path.exists(build_file):
        print("Error: 'build.xml' not found in the same directory as this script.")
        return

    cmd_clean = ['ant', 'clean']
    cmd_build = ['ant', 'compile']

    if len(sys.argv) == 2:
        if sys.argv[1] == 'clean':
            print("Running 'ant clean'")
            subprocess.run(cmd_clean, shell=True, text=True)
            return
        elif sys.argv[1] == 'build':
            print("Running 'ant compile'")
            subprocess.run(cmd_build, shell=True, text=True)
            return
        else:
            print("Usage: build | clean | <int/float> <add/sub/mul/div> <operand1> <operand2>")
            return

    if len(sys.argv) != 5:
        print("Usage: python run_project.py <int/float> <add/sub/mul/div> <operand1> <operand2>")
        return

    dtype, operation, op1, op2 = sys.argv[1:]

    # Validate dtype
    if dtype not in ('int', 'float'):
        print(f"Error: Expected 'float' or 'int', got {dtype}")
        return

    # Validate operation
    if operation not in ('add', 'sub', 'mul', 'div'):
        print(f"Error: Expected 'add' or 'sub' or 'mul' or 'div', got {operation}")
        return

    # Validate operands
    if not is_valid_number(op1, dtype) or not is_valid_number(op2, dtype):
        print(f"Error: Expected {dtype} values, got {op1} or {op2}")
        return

    cmd_run = ['ant', 'run', f'-Dargs={dtype} {operation} {op1} {op2}']
    print(f"Running the program with the command: {' '.join(cmd_run)}")
    build_dir = os.path.join(os.path.dirname(__file__), "build")

    if not os.path.isdir(build_dir):
        print("\'Build\' directory not found. Building the project...")
        subprocess.run(cmd_build, shell=True, text=True)

    print("Output:")
    result = subprocess.run(cmd_run, capture_output=True, text=True, shell=True)
    print(result.stdout)

if __name__ == "__main__":
    main()
