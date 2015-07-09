package pong;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import network.NeuralEvolution;
import network.Parameters;
import network.nodes.Node;

import java.util.ArrayList;

@EqualsAndHashCode(callSuper = false)
@Data
public class Pong extends Game {
    static int outputNodeCount;

    static ArrayList<InputNode> inputNodes = new ArrayList<InputNode>();
    static ArrayList<OutputNode> outputNodes = new ArrayList<OutputNode>();
    Field field;
    boolean finished;

    NeuralEvolution ai = new NeuralEvolution();
    ArrayList<Integer> scores = new ArrayList<Integer>();

    public static void main(String[] args) {
        Field.width = 1200;
        Field.height = 720;

        Ball.speed = 2000;
        Paddle.speed = 2100;

        outputNodeCount = 2;
        createNodes();

        Parameters.inputNodes = Input.values().length;
        Parameters.outputNodes = outputNodes.size();
        Parameters.initialPopulation = 10;
        Parameters.c1 = 1;
        Parameters.c2 = 1;
        Parameters.c3 = 0.4;
        Parameters.delta = 3;
        Parameters.epsilon = 0.1;
        Parameters.fireNeuron = 0.5;
        Parameters.survivalRate = 0.9; // kill off 10% of the fit population
        Parameters.weightMutation = 0.8;
        Parameters.uniformWeightModifier = 0.2;
        Parameters.randomMutate = 0.2;
        Parameters.newNode = 0.10;
        Parameters.newGene = 0.2;
        Parameters.newNodeCound = 2;
        Parameters.newGeneCount = 3;
        Parameters.disabled = 0.75;
        Parameters.noCrossing = 0.05;
        Parameters.interSpecies = 0.001;


        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.width = Field.width;
        cfg.height = Field.height;
        cfg.vSyncEnabled = true;

        Pong pong = new Pong();
        new LwjglApplication(pong, cfg);
    }

    private static void createNodes() {
        for (Input i : Input.values()) {
            inputNodes.add(new InputNode(i));
        }

        for (int i = 0; i < outputNodeCount; i++) {
            outputNodes.add(new OutputNode());
        }
    }

    private static void clearNodes() {
        for (OutputNode outputNode : outputNodes) {
            outputNode.clear();
        }
    }

    @Override
    public void create() {
        field = new Field(inputNodes, outputNodes, this);

        for (InputNode in : inputNodes) {
            in.setBall(field.getBall());
            in.setP1(field.getP1());
            in.setP2(field.getP2());
        }

        setScreen(field);
    }

    public int getFitness() {
        int fitness = field.getBall().getHits() * 10000;

        if (field.getP2().isWinner()) {
            fitness *= 100;
        }

        return fitness;
    }

    public void nextRound() {
        setScreen(null);

        scores.add(getFitness());

        if (scores.size() == 3) {
            ai.results((scores.get(0) + scores.get(1) + scores.get(2)) / 3);
            scores.clear();
            clearNodes();
            ai.nextRound(new ArrayList<Node>(inputNodes), new ArrayList<Node>(outputNodes));
        }

        create();
    }
}
