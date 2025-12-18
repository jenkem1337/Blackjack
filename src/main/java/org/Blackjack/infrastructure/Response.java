package org.Blackjack.infrastructure;

public sealed interface Response permits NullResponse, ApplicationErrorResponse, SuccessResponse, SystemErrorResponse{

}
