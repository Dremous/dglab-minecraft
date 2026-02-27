package com.dglab.minecraft;

public enum WaveType {
    STABLE {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            return new int[]{baseFreq, baseFreq, baseFreq, baseFreq};
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            return new int[]{baseStrength, baseStrength, baseStrength, baseStrength};
        }
    },
    IMPACT {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int high = baseFreq + 30;
            int mid = baseFreq + 15;
            return new int[]{high, mid, baseFreq, baseFreq - 10};
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            int peak = (int)(baseStrength * 1.3);
            return new int[]{peak, baseStrength, (int)(baseStrength * 0.8), (int)(baseStrength * 0.6)};
        }
    },
    BURNING {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int variation = (int)(Math.sin(pulseIndex * 0.5) * 15);
            return new int[]{
                baseFreq + variation,
                baseFreq + variation + 5,
                baseFreq + variation + 10,
                baseFreq + variation + 5
            };
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            int variation = (int)(Math.sin(pulseIndex * 0.8) * baseStrength * 0.2);
            int base = baseStrength + variation;
            return new int[]{base, base + 5, base, base - 5};
        }
    },
    SHARP {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int high = baseFreq + 50;
            return new int[]{high, high, baseFreq, baseFreq};
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            int spike = (int)(baseStrength * 1.5);
            return new int[]{spike, (int)(baseStrength * 0.5), 0, 0};
        }
    },
    ELECTRIC {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int rand1 = (int)(Math.random() * 40);
            int rand2 = (int)(Math.random() * 40);
            return new int[]{
                baseFreq + rand1,
                baseFreq + rand2,
                baseFreq + rand1,
                baseFreq + rand2
            };
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            int rand1 = (int)(Math.random() * baseStrength * 0.4);
            int rand2 = (int)(Math.random() * baseStrength * 0.4);
            return new int[]{
                baseStrength + rand1,
                baseStrength - rand2,
                baseStrength + rand2,
                baseStrength - rand1
            };
        }
    },
    FLUCTUATE {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            double wave = Math.sin(pulseIndex * 0.3);
            int variation = (int)(wave * 30);
            return new int[]{
                baseFreq + variation,
                baseFreq + variation + 10,
                baseFreq + variation,
                baseFreq + variation - 10
            };
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            double wave = Math.sin(pulseIndex * 0.5);
            int variation = (int)(wave * baseStrength * 0.3);
            return new int[]{
                baseStrength + variation,
                baseStrength + variation + 5,
                baseStrength + variation,
                baseStrength + variation - 5
            };
        }
    },
    GRADUAL {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int progress = Math.min(pulseIndex * 5, 50);
            return new int[]{
                Math.min(240, baseFreq + progress),
                Math.min(240, baseFreq + progress + 3),
                Math.min(240, baseFreq + progress + 6),
                Math.min(240, baseFreq + progress + 9)
            };
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            double progress = (double)pulseIndex / totalPulses;
            int currentStrength = (int)(baseStrength * (0.5 + progress * 0.5));
            currentStrength = Math.min(100, currentStrength);
            return new int[]{
                currentStrength,
                Math.min(100, currentStrength + 3),
                Math.min(100, currentStrength + 6),
                Math.min(100, currentStrength + 9)
            };
        }
    },
    PULSE {
        @Override
        public int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses) {
            int mod = pulseIndex % 2;
            int freq = mod == 0 ? baseFreq + 20 : baseFreq - 10;
            return new int[]{freq, freq, freq, freq};
        }
        
        @Override
        public int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses) {
            int mod = pulseIndex % 2;
            int strength = mod == 0 ? (int)(baseStrength * 1.2) : (int)(baseStrength * 0.3);
            return new int[]{strength, strength, strength, strength};
        }
    };
    
    public abstract int[] getFrequencies(int baseFreq, int pulseIndex, int totalPulses);
    public abstract int[] getStrengths(int baseStrength, int pulseIndex, int totalPulses);
}
