package examples;

import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class ArtCreateFlowInitiator extends FlowLogic<SignedTransaction> {
    private final String title;
    private final String artist;

    public ArtCreateFlowInitiator(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    private final ProgressTracker.Step INICIALISING = new ProgressTracker.Step("Inicialising");
    private final ProgressTracker.Step BUILDING = new ProgressTracker.Step("Building");
    private final ProgressTracker.Step SIGNING = new ProgressTracker.Step("Signing");

    private final ProgressTracker progressTracker = new ProgressTracker(INICIALISING, BUILDING, SIGNING);

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    public SignedTransaction call() throws FlowException {
        progressTracker.setCurrentStep(INICIALISING);

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().stream().findFirst().orElseThrow(() -> new FlowException("Notary not found"));

        TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        Party party = getOurIdentity();

        progressTracker.setCurrentStep(BUILDING);

        ArtState outputArtState = new ArtState(
                this.artist,
                this.title,
                party,
                party);
        txBuilder.addOutputState(outputArtState, ArtContract.ID);

        ArtContract.Commands.Issue commandData = new ArtContract.Commands.Issue();
        List<PublicKey> requiredSigners = ImmutableList.of(
                party.getOwningKey()
        );
        txBuilder.addCommand(commandData, requiredSigners);

        txBuilder.verify(getServiceHub());

        progressTracker.setCurrentStep(SIGNING);

        SignedTransaction fullySignedTx = getServiceHub().signInitialTransaction(txBuilder);

        return subFlow(new FinalityFlow(fullySignedTx, new ArrayList()));
    }
}
