RWStructuredBuffer<float4> Output : register( b0 );

cbuffer VariablesBuffer : register( b1 ) {
    float width;
    float height;
}

double getClosestT(double3 origin, double3 direction, double radius) {
    double a = dot(direction, direction);
    double b = 2 * dot(origin, direction);
    double c = dot(origin, origin) - radius * radius;

    double discriminator = b * b - 4 * a * c;
    if (discriminator < 0) {
        return - 1;
    }

    float nominator = -b - sqrt(discriminator);

    return nominator / (2 * a);
}

[numthreads(8,8,1)]
void MainEntry (uint3 id : SV_DispatchThreadID)
{
    double x = id.x / width - 0.5;
    double y = id.y / height - 0.5;

    double3 sphereOrigin = double3(0, 0, 10);
    double sphereRadius = 3;

    double t = getClosestT(-sphereOrigin, double3(x, y, 1), sphereRadius);

    if (t < 0) {
        Output[id.y * width + id.x] = float4(0, 0, 0, 255);
    }
    else {
        Output[id.y * width + id.x] = float4(255, 255, 255, 255);
    }

}