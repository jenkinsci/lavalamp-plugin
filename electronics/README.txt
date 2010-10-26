
FTDI datasheets are available from: 
http://www.ftdichip.com/Documents/Datasheets.htm
FT232R Datasheet
D2XX Programmer's Guide
http://www.ftdichip.com/Documents/AppNotes/AN232R-01_FT232RBitBangModes.pdf 


Program MM232R using FT_PROG.EXE
Use ft-mm232r-lavalam.xml as the template
Important parameters to change are:

1) Set "Load D2XX Driver"="true"

2) Set "Invert RS232 Signals"="true" for:
	TxD
	TxD
	CTS
	RTS
	
3) Set I/O Controls = I/O mode for:
	C0
	C1
	C2
	
4) Leave I/O Controls for C3 = PWRON#

5) DO NOT set "ExternalOscillator"="true" 
- doing so without an external oscillator source will render the device unusable!


