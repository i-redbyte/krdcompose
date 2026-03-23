package ru.redbyte.krdcompose.screens.list.hackerList

data class HackerNodeUi(
    val id: Int,
    val title: String,
    val status: String,
    val shortInfo: String,
    val details: String
)

object HackerNodeFactory {

    fun createList(): List<HackerNodeUi> {
        return listOf(
            HackerNodeUi(
                id = 1,
                title = "NODE_01",
                status = "ONLINE",
                shortInfo = "Encrypted channel established",
                details = "Handshake complete. Tunnel integrity 98%. Packet loss below threshold. Awaiting operator commands."
            ),
            HackerNodeUi(
                id = 2,
                title = "NODE_02",
                status = "SCANNING",
                shortInfo = "Port sweep in progress",
                details = "Target subnet mapped. Open endpoints detected on 22, 443, 8080. Fingerprinting modules still running."
            ),
            HackerNodeUi(
                id = 3,
                title = "NODE_03",
                status = "LOCKED",
                shortInfo = "Access level restricted",
                details = "Credential escalation required. Existing token lacks permission for deep inspection and remote execution."
            ),
            HackerNodeUi(
                id = 4,
                title = "NODE_04",
                status = "SYNCED",
                shortInfo = "Mirror cache updated",
                details = "Remote snapshots merged. Diff size reduced by delta compression. Integrity checksum verified."
            ),
            HackerNodeUi(
                id = 5,
                title = "NODE_05",
                status = "WATCHING",
                shortInfo = "Anomaly triggers enabled",
                details = "Behavioral hooks armed. Drift monitor active. Latency spikes above normal baseline will be reported."
            )
        )
    }
}