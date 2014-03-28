JFLAGS = -g -cp '.:acm.jar'
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
        Simulator.java \
        Light.java \
        City.java \
        GraphicsCity.java \
        WaitingRegressionLight.java \
        RegressionLight.java \
        ConstantLight.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
