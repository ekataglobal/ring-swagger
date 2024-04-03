(ns ring.swagger.json-schema-dirty
  "JsonSchema conversions for some Schemas which can't be
  properly described using the Swagger Schema."
  (:require [ring.swagger.json-schema :as json-schema]
            [schema.experimental.abstract-map]))

(extend-protocol json-schema/JsonSchema
  schema.experimental.abstract_map.AbstractSchema
  (convert [e {:keys [properties? schema-type]
               :or {properties? true}} ]
    (if properties?
      (merge {:discriminator (name (:dispatch-key e))}
             (json-schema/->swagger (:schema e) {:properties? properties? :schema-type schema-type}))
      (json-schema/reference e schema-type)))

  schema.experimental.abstract_map.SchemaExtension
  (convert [e {:keys [schema-type] :as options}]
    {:allOf [(json-schema/->swagger (:base-schema e) {:properties? false :schema-type schema-type})
             ; Find which keys are also in base-schema and don't include them in these properties
             (json-schema/->swagger (let [base-keys (set (keys (:schema (:base-schema e))))
                                          m         (:extended-schema e)]
                                      (into (empty m) (remove (comp base-keys key) m)))
                                    {:properties? true :schema-type schema-type})]}))
