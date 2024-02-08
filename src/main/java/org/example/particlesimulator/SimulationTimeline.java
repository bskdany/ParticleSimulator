package org.example.particlesimulator;


import java.util.LinkedList;
import java.util.List;

public class SimulationTimeline {
    private static final int simulationDataToSave = 100;
    public static final int timeToSaveMs = 200; // 200 * 100 = 20 seconds saved each time
    public long lastSaveMs = 0;
    private long lastRestoreTime = 0;
    private int newOffset = 0;
    List<ParticleSimulationData> simulationTimeline = new LinkedList<ParticleSimulationData>();

    public void add(ParticleSimulationData simulationData){
        simulationTimeline.add(simulationData);
        lastSaveMs = System.currentTimeMillis();
        if(simulationTimeline.size() > simulationDataToSave){
            simulationTimeline.removeFirst();
        }
    }
    public ParticleSimulationData getAt(int offset){
        // if offset is too big
        if(offset > simulationTimeline.size()){
            newOffset = simulationTimeline.size();
            return simulationTimeline.getFirst();
        }
        // if offset is 0
        else if (offset == 0) {
            newOffset = 0;
            return simulationTimeline.getLast();
        }
        // offset is in the range
        else{
            newOffset = offset;
            return simulationTimeline.get(simulationTimeline.size() - offset);
        }
    }

    public void setNewAtCurrent(){
        System.out.println(simulationTimeline.size());

        for (int i = 0; i < newOffset; i++) {
            simulationTimeline.removeLast();
        }
        newOffset = 0;
        lastSaveMs = System.currentTimeMillis();
        System.out.println(simulationTimeline.size());
    }
}
