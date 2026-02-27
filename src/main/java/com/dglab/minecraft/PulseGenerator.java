package com.dglab.minecraft;

import java.nio.ByteBuffer;

public class PulseGenerator {
    
    public static String[] generatePulse(int channel, int strength, int frequency, int durationMs) {
        return generatePulse(channel, strength, frequency, durationMs, WaveType.STABLE);
    }
    
    public static String[] generatePulse(int channel, int strength, int frequency, int durationMs, WaveType waveType) {
        int pulseCount = Math.max(1, durationMs / 100);
        String[] pulses = new String[pulseCount];
        
        for (int i = 0; i < pulseCount; i++) {
            int[] freqs = waveType.getFrequencies(frequency, i, pulseCount);
            int[] strengths = waveType.getStrengths(strength, i, pulseCount);
            pulses[i] = generateB0Pulse(channel, freqs, strengths);
        }
        
        return pulses;
    }
    
    public static String buildPulseArrayJson(String[] pulseData) {
        if (pulseData == null || pulseData.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pulseData.length; i++) {
            sb.append('"').append(pulseData[i]).append('"');
            if (i < pulseData.length - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    public static String buildStrengthMessage(int channel, int mode, int value) {
        return "strength-" + channel + "+" + mode + "+" + value;
    }
    
    public static String getChannelName(int channel) {
        return channel == 1 ? "A" : "B";
    }
    
    private static String generateB0Pulse(int channel, int[] frequencies, int[] strengths) {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        
        buffer.put((byte) 0xB0);
        
        buffer.put((byte) 0x00);
        
        int modeByte = 0x00;
        if (channel == 1) {
            modeByte = 0b1100;
        } else if (channel == 2) {
            modeByte = 0b0011;
        } else {
            modeByte = 0b1111;
        }
        buffer.put((byte) modeByte);
        
        int avgStrength = 0;
        for (int s : strengths) {
            avgStrength += Math.min(100, Math.max(0, s));
        }
        avgStrength = avgStrength / strengths.length;
        
        if (channel == 1 || channel == 0) {
            buffer.put((byte) avgStrength);
        } else {
            buffer.put((byte) 0);
        }
        
        if (channel == 2 || channel == 0) {
            buffer.put((byte) avgStrength);
        } else {
            buffer.put((byte) 0);
        }
        
        for (int i = 0; i < 4; i++) {
            int freq = (i < frequencies.length) ? Math.min(240, Math.max(10, frequencies[i])) : 50;
            buffer.put((byte) freq);
        }
        
        for (int i = 0; i < 4; i++) {
            int str = (i < strengths.length) ? Math.min(100, Math.max(0, strengths[i])) : 50;
            buffer.put((byte) str);
        }
        
        if (channel == 1) {
            for (int i = 0; i < 4; i++) {
                buffer.put((byte) 0);
            }
            for (int i = 0; i < 4; i++) {
                buffer.put((byte) 0xFF);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                buffer.put((byte) 0);
            }
            for (int i = 0; i < 4; i++) {
                buffer.put((byte) 0xFF);
            }
        }
        
        byte[] bytes = buffer.array();
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        
        return hex.toString();
    }
    
    private static String generateB0Pulse(int channel, int strength, int frequency) {
        int[] freqs = {frequency, frequency, frequency, frequency};
        int[] strengths = {strength, strength, strength, strength};
        return generateB0Pulse(channel, freqs, strengths);
    }
    
    public static String generateBFPulse(int channel, int softLimit, int freqBalance, int strengthBalance) {
        ByteBuffer buffer = ByteBuffer.allocate(7);
        
        buffer.put((byte) 0xBF);
        
        if (channel == 1) {
            buffer.put((byte) softLimit);
            buffer.put((byte) 200);
        } else {
            buffer.put((byte) 200);
            buffer.put((byte) softLimit);
        }
        
        buffer.put((byte) (freqBalance & 0xFF));
        buffer.put((byte) ((freqBalance >> 8) & 0xFF));
        buffer.put((byte) (strengthBalance & 0xFF));
        buffer.put((byte) ((strengthBalance >> 8) & 0xFF));
        
        byte[] bytes = buffer.array();
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b & 0xFF));
        }
        
        return hex.toString();
    }
    
    public static String[] generateDamagePulse(float damage, DamageType damageType) {
        int strength = calculateStrength(damage, damageType);
        int duration = calculateDuration(damage, damageType);
        int frequency = calculateFrequency(damage, damageType);
        
        return generatePulse(DGLabConfig.channel.get(), strength, frequency, duration, damageType.waveType);
    }
    
    public static String[] generateDamagePulse(float damage) {
        return generateDamagePulse(damage, DamageType.GENERIC);
    }
    
    private static int calculateStrength(float damage, DamageType damageType) {
        int base = (int)(damageType.baseStrength * damageType.strengthMultiplier);
        int calculated = (int)(base + (damage * 5 * DGLabConfig.damageMultiplier));
        return Math.min(DGLabConfig.maxStrength, Math.max(DGLabConfig.baseStrength, calculated));
    }
    
    private static int calculateDuration(float damage, DamageType damageType) {
        int base = (int)(100 * damageType.durationMultiplier);
        int calculated = (int)(base + (damage * 30 * DGLabConfig.damageMultiplier));
        return Math.min(DGLabConfig.maxDuration, Math.max(DGLabConfig.baseDuration, calculated));
    }
    
    private static int calculateFrequency(float damage, DamageType damageType) {
        return DGLabConfig.baseFrequency + Math.min(50, (int)(damage * 5));
    }
}
