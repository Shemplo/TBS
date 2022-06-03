package ru.shemplo.tbs.entity;

import java.util.function.ToDoubleFunction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.utils.MapperUtils;

@Getter
@RequiredArgsConstructor
public enum OperationTypeCategory {
    
    TAX            ("Tax",       op -> MapperUtils.moneyValueToBigDecimal (op.getPayment ()).doubleValue ()),
    FEE            ("Fee",       op -> MapperUtils.moneyValueToBigDecimal (op.getPayment ()).doubleValue ()),
    BOND_BUY       ("Purchase",  op -> (op.getQuantity () - op.getQuantityRest ()) 
                                     * -MapperUtils.moneyValueToBigDecimal (op.getPrice ()).doubleValue ()),
    BOND_COUPON    ("Coupons",   op -> MapperUtils.moneyValueToBigDecimal (op.getPayment ()).doubleValue ()),
    BOND_REPAYMENT ("Repayment", op -> MapperUtils.moneyValueToBigDecimal (op.getPayment ()).doubleValue ()),
    BOND_SELL      ("Sell",      op -> (op.getQuantity () - op.getQuantityRest ()) 
                                     * MapperUtils.moneyValueToBigDecimal (op.getPrice ()).doubleValue ());
    
    private final String text;
    
    private final ToDoubleFunction <Operation> sumEffectFetcher;
    
    public double getSumValue (Operation operation) {
        return sumEffectFetcher.applyAsDouble (operation);
    }
    
}
