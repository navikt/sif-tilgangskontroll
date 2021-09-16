package no.nav.siftilgangskontroll.spesification

data class Policy<T>(
    val id: String = "",
    val description: String,
    val children: List<Policy<T>> = emptyList(),
    val evaluation: T.() -> PolicyEvaluation
) {
    fun evaluate(t: T): PolicyEvaluation {
        return PolicyEvaluation.evaluate(
            id = id,
            description = description,
            eval = t.evaluation()
        )
    }

    infix fun and(other: Policy<T>): Policy<T> {
        return Policy(
            description = "$description AND ${other.description}",
            children = this.specOrChildren() + other.specOrChildren(),
            evaluation = { evaluate(this) and other.evaluate(this) }
        )
    }

    infix fun or(other: Policy<T>): Policy<T> {
        return Policy(
            description = "$description OR ${other.description}",
            children = this.specOrChildren() + other.specOrChildren(),
            evaluation = { evaluate(this) or other.evaluate(this) }
        )
    }

    fun not(): Policy<T> {
        return Policy(
            description = "!$description",
            id = "!$id",
            children = listOf(this),
            evaluation = { evaluate(this).not() }
        )
    }

    fun with(id: String, description: String): Policy<T> {
        return this.copy(id = id, description = description)
    }

    private fun specOrChildren(): List<Policy<T>> =
        if (id.isBlank() && children.isNotEmpty()) children else listOf(this)

    private constructor(builder: Builder<T>) : this(builder.id, builder.description, builder.children, builder.evaluation)

    companion object {
        inline fun <T> policy(block: Builder<T>.() -> Unit) = Builder<T>().apply(block).build()
    }

    class Builder<T> {
        var id: String = ""
        var description: String = ""
        var children: List<Policy<T>> = emptyList()
        var evaluation: T.() -> PolicyEvaluation = { PolicyEvaluation.deny("not implemented") }
        fun build() = Policy(this)
    }
}

fun <T> not(spec: Policy<T>) = spec.not()

inline fun <T, R> Policy<T>.requirePermitOrFail(ctx: T, block: (PolicyEvaluation) -> R): R =
    this.evaluate(ctx).let {
        when (it.decision) {
            PolicyDecision.PERMIT -> block.invoke(it)
            else -> throw PolicyEvaluationException(it)
        }
    }

inline fun <T, R> Policy<T>.requirePermit(ctx: T, block: () -> R): Any =
    this.evaluate(ctx).let {
        when (it.decision) {
            PolicyDecision.PERMIT -> block.invoke()
            else -> it
        }
    } ?: PolicyEvaluation.deny("could not evaluate policy")

inline fun <T, R> requirePermitOrFail(ctx: T, policy: Policy<T>, block: (PolicyEvaluation) -> R): R = policy.requirePermitOrFail(ctx, block)

inline fun <T, R> requirePermit(ctx: T, policy: Policy<T>, block: () -> R) = policy.requirePermit(ctx, block)

inline fun <T, R> evaluate(ctx: T, policy: Policy<T>, block: (PolicyEvaluation) -> R?): R? =
    policy.evaluate(ctx).let {
        block.invoke(it)
    }

inline fun <T, R> authorize(ctx: T, policy: Policy<T>, block: (PolicyEvaluation) -> R?): R? =
    policy.requirePermitOrFail(ctx, block)

data class PolicyEvaluationException(
    val evaluation: PolicyEvaluation,
    val t: Throwable? = null,
) : RuntimeException("policy evaluation failed with reason=${evaluation.reason}", t)
