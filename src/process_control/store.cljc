(ns process-control.store
  "ProcessControlStore protocol — backing store for process control
  technician operations coordination: registered units/systems, committed
  operation records, and append-only audit ledger.

  A minimal implementation holds all state in memory (MemStore).
  Production implementations will implement the Store protocol against
  a durable backend (e.g. datomic).")

(defprotocol Store
  "A backing data store for process control operations coordination."
  (unit [store unit-id]
    "Look up a registered process control unit/system by ID. Returns
    `{:unit-id .. :name .. :status .. :verified? ..}` or nil if not found.")
  (commit-record! [store record]
    "Atomically commit an operation record: `{:unit-id .. :op
    .. :payload ..}`. The store must persist the record before returning.
    Throws on error.")
  (append-ledger! [store entry]
    "Atomically append an audit ledger entry to the immutable ledger.
    Entry shape: `{:disposition :commit|:hold|:escalate :record|:verdict ..}`.")
  (records [store]
    "Return the seq of committed operation records.")
  (ledger [store]
    "Return the full append-only audit ledger (seq of entries)."))

(defrecord MemStore [units records-ref ledger-ref]
  Store
  (unit [_ unit-id]
    (get units unit-id))
  (commit-record! [_ record]
    (when-not (:unit-id record)
      (throw (ex-info "commit-record! requires :unit-id" {:record record})))
    (swap! records-ref conj record))
  (append-ledger! [_ entry]
    (swap! ledger-ref conj entry))
  (records [_]
    @records-ref)
  (ledger [_]
    @ledger-ref))

(defn mem-store
  "Create an in-memory MemStore with optional initial units.
  `units-map` is `{:unit-id {:name .. :status .. :verified? true}}`."
  ([] (mem-store {}))
  ([units-map]
   (MemStore. units-map (atom []) (atom []))))
