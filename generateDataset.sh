for i in {1..10000000}; do
    a=$RANDOM;
    curl -XPUT http://localhost:9200/example/doc/$i -d '{
        "name" : "doc '$i'",
        "a" : '$a'
    }'
done
