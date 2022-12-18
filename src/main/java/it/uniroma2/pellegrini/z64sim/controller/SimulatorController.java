/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.model.CpuState;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.components.RegisterBank;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class SimulatorController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();
    private static SimulatorController instance;

    private final CpuState cpuState = new CpuState();
    private Program program;
    private RegisterBank cpuView;

    private SimulatorController() {
    }

    public static void init() {
        instance = new SimulatorController();
    }

    private static SimulatorController getInstance() {
        if(instance == null)
            init();
        return instance;
    }

    public static void setCpuView(RegisterBank cpuView) {
        getInstance().cpuView = cpuView;
    }

    public static void displaceRIP(int displacement) {
        long rip = getInstance().cpuState.getRIP();
        setRIP(rip + displacement);

    }

    public static Long getRIP() {
        return getInstance().cpuState.getRIP();
    }


    // TODO: move to a dedicated class
    public static long computeAddressingMode(OperandMemory op) {
        final int displacement = op.getDisplacement();
        final long base = op.getBase(); // TODO: long?!
        final int index = op.getIndex();
        final int scale = op.getScale();

        final Long baseValue = getInstance().cpuState.getRegisterValue((int) base);
        final Long indexValue = getInstance().cpuState.getRegisterValue(index);

        long address = indexValue == null ? 0 : indexValue * scale;
        address += baseValue == null ? 0 : baseValue;
        address += displacement;
        return address;
    }

    public static Long getOperandValue(Operand op) {
        if(op instanceof OperandRegister) {
            return getInstance().cpuState.getRegisterValue(((OperandRegister) op).getRegister());
        }
        if(op instanceof OperandImmediate) {
            return ((OperandImmediate) op).getValue();
        }
        if(op instanceof OperandMemory) {
            long address = computeAddressingMode((OperandMemory) op);

            long memoryValue = 0;
            switch(op.getSize()) {
                case 1:
                    memoryValue = Memory.getValueAt(address);
                    break;
                case 2:
                    memoryValue = Memory.getValueAt(address) | (Memory.getValueAt(address + 1) << 8);
                    break;
                case 4:
                    memoryValue = Memory.getValueAt(address) | (Memory.getValueAt(address + 1) << 8)
                        | (Memory.getValueAt(address + 2) << 16) | (Memory.getValueAt(address + 3) << 24);
                    break;
                case 8:
                    memoryValue = Memory.getValueAt(address) | (Memory.getValueAt(address + 1) << 8)
                        | (Memory.getValueAt(address + 2) << 16) | (Memory.getValueAt(address + 3) << 24)
                        | ((long) Memory.getValueAt(address + 4) << 32) | ((long) Memory.getValueAt(address + 5) << 40)
                        | ((long) Memory.getValueAt(address + 6) << 48) | ((long) Memory.getValueAt(address + 7) << 56);
                    break;
            }

            return memoryValue;
        }

        return null;
    }

    public static void setOperandValue(Operand destination, Long srcValue) {
        if(destination instanceof OperandRegister) {
            getInstance().cpuState.setRegisterValue(((OperandRegister) destination).getRegister(), srcValue);
        }
        if(destination instanceof OperandMemory) {
            long address = computeAddressingMode((OperandMemory) destination);

            switch(destination.getSize()) {
                case 1:
                    Memory.setValueAt(address, srcValue.byteValue());
                    break;
                case 2:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    break;
                case 4:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    Memory.setValueAt(address + 2, (byte) (srcValue >> 16));
                    Memory.setValueAt(address + 3, (byte) (srcValue >> 24));
                    break;
                case 8:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    Memory.setValueAt(address + 2, (byte) (srcValue >> 16));
                    Memory.setValueAt(address + 3, (byte) (srcValue >> 24));
                    Memory.setValueAt(address + 4, (byte) (srcValue >> 32));
                    Memory.setValueAt(address + 5, (byte) (srcValue >> 40));
                    Memory.setValueAt(address + 6, (byte) (srcValue >> 48));
                    Memory.setValueAt(address + 7, (byte) (srcValue >> 56));
                    break;
            }
            Memory.selectAddress(address);
        }
    }

    public static void setCF(boolean value) {
        getInstance().cpuState.setCF(value);
    }

    public static void setZF(boolean value) {
        getInstance().cpuState.setZF(value);
    }

    public static void setSF(boolean value) {
        getInstance().cpuState.setSF(value);
    }

    public static void setOF(boolean value) {
        getInstance().cpuState.setOF(value);
    }

    public static void setPF(boolean value) {
        getInstance().cpuState.setPF(value);
    }

    public static void setDF(boolean value) {
        getInstance().cpuState.setDF(value);
    }

    public static void setIF(boolean value) {
        getInstance().cpuState.setIF(value);
    }

    public static boolean getCF() {
        return getInstance().cpuState.getCF();
    }

    public static boolean getZF() {
        return getInstance().cpuState.getZF();
    }

    public static boolean getSF() {
        return getInstance().cpuState.getSF();
    }

    public static boolean getOF() {
        return getInstance().cpuState.getOF();
    }

    public static boolean getPF() {
        return getInstance().cpuState.getPF();
    }

    public static boolean getDF() {
        return getInstance().cpuState.getDF();
    }

    public static boolean getIF() {
        return getInstance().cpuState.getIF();
    }

    private void assembleProgram() {
        String code = MainWindow.getCode();

        Assembler a = new Assembler(code);
        try {
            a.Program();
        } catch(ParseException ignored) {
        }

        List<String> syntaxErrors = new ArrayList<>(a.getSyntaxErrors());

        StringBuilder assemblerOutput = new StringBuilder();
        if(syntaxErrors.isEmpty()) {
            assemblerOutput.append("Assembly successful.");
        } else {
            assemblerOutput.append("Assembly failed with ").append(syntaxErrors.size()).append(" errors:\n\n");
            for(String e : syntaxErrors) {
                assemblerOutput.append(e).append("\n");
            }
        }

        MainWindow.compileResult(assemblerOutput.toString());
        this.program = a.getProgram();
        Memory.setProgram(this.program);

        long _start = this.program._start.getTarget();
        this.setRIP(_start);
    }

    public static boolean step() {
        return getInstance().stepInstruction();
    }

    public static void run() {
        boolean hlt = false;
        do {
            hlt = getInstance().stepInstruction();
        } while(!hlt);
    }

    // Return true if the program reached a halt instruction
    private boolean stepInstruction() {
        SimulatorController sc = getInstance();
        if(sc.program == null)
            return false;

        Instruction instruction = sc.fetch();

        try {
            instruction.run();

            if(instruction.getMnemonic().equals("hlt")) {
                return true;
            }
        } catch(RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    private Instruction fetch() {
        long rip = this.cpuState.getRIP();
        Instruction instruction = (Instruction) this.program.getMemoryElementAt(rip);
        setRIP(rip + instruction.getSize());

        return instruction;
    }

    public static void setRIP(long address) {
        getInstance().cpuState.setRIP(address);
        getInstance().cpuView.setRIP(address);
        Memory.selectAddress(address);
    }

    @Override
    public boolean dispatch(Events command) {
        switch(command) {
            case ASSEMBLE_PROGRAM:
                assembleProgram();
                break;
        }
        return false;
    }
}