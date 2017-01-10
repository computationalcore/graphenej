package de.bitsharesmunich.graphenej.errors;

/**
 * Created by nelson on 12/25/16.
 */
public class IncompleteAssetError extends RuntimeException{

    public IncompleteAssetError(String message){
        super(message);
    }

    public IncompleteAssetError(){
        super("The asset used in this method is probably incomplete, Assets instances can be created with just its id information but this context requires more information");
    }
}
