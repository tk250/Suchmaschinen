DOCTEST_CMD = python3 -m doctest
COMPILE_CMD = python3 -m py_compile
CHECKSTYLE_CMD = flake8

all: compile test checkstyle

compile:
	$(COMPILE_CMD) *.py

test:
	$(DOCTEST_CMD) *.py

checkstyle:
	$(CHECKSTYLE_CMD) *.py

clean:
	rm -f *.pyc
	rm -rf __pycache__
