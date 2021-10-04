package no.nav.policy.spesification

data class PolicyEvaluation(
    val decision: PolicyDecision,
    val reason: String,
    val description: String = "",
    val id: String = "",
    val operator: Operator = Operator.NONE,
    var children: List<PolicyEvaluation> = emptyList()
) {

    infix fun and(other: PolicyEvaluation) = PolicyEvaluation(
        decision = decision and other.decision,
        reason = "($reason AND ${other.reason})",
        operator = Operator.AND,
        children = this.specOrChildren() + other.specOrChildren()
    )

    infix fun or(other: PolicyEvaluation) = PolicyEvaluation(
        decision = decision or other.decision,
        reason = "($reason OR ${other.reason})",
        operator = Operator.OR,
        children = this.specOrChildren() + other.specOrChildren()
    )

    operator fun not() = PolicyEvaluation(
        decision = decision.not(),
        reason = "(NOT $reason)",
        operator = Operator.NOT,
        children = listOf(this)
    )

    private fun specOrChildren(): List<PolicyEvaluation> =
        if (id.isBlank() && children.isNotEmpty()) children else listOf(this)

    companion object {
        fun permit(reason: String = "") = PolicyEvaluation(PolicyDecision.PERMIT, reason)
        fun deny(reason: String) = PolicyEvaluation(PolicyDecision.DENY, reason)

        fun notApplicable(reason: String) = PolicyEvaluation(PolicyDecision.NOT_APPLICABLE, reason)

        fun evaluate(id: String, description: String, eval: PolicyEvaluation) = eval.copy(id = id, description = description)
    }
}
fun PolicyEvaluation.isPermit(): Boolean = this.decision == PolicyDecision.PERMIT
fun PolicyEvaluation.isDeny(): Boolean = this.decision == PolicyDecision.DENY

infix fun PolicyEvaluation.equalTo(decision: PolicyDecision): Boolean = this.decision == decision
infix fun PolicyEvaluation.notEqualTo(decision: PolicyDecision): Boolean = this.decision != decision
